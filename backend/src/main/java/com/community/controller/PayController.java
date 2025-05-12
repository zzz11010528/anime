package com.community.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.community.common.result.R;
import com.community.common.util.AlipayUtil;
import com.community.entity.Order;
import com.community.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制器
 */
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PayController {

    private final OrderService orderService;
    private final AlipayUtil alipayUtil;

    /**
     * 支付宝支付异步回调
     */
    @PostMapping("/notify")
    public String alipayNotify(HttpServletRequest request) {
        // 获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        try {
            // 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayUtil.getAlipayPublicKey(),
                    "UTF-8",
                    "RSA2");

            if (signVerified) {
                // 验证成功
                // 商户订单号
                String outTradeNo = params.get("out_trade_no");
                // 支付宝交易号
                String tradeNo = params.get("trade_no");
                // 交易状态
                String tradeStatus = params.get("trade_status");
                // 交易金额
                String totalAmount = params.get("total_amount");

                // 记录回调参数，方便调试
                System.out.println("支付宝异步回调 - 订单号: " + outTradeNo);
                System.out.println("支付宝异步回调 - 交易号: " + tradeNo);
                System.out.println("支付宝异步回调 - 交易状态: " + tradeStatus);
                System.out.println("支付宝异步回调 - 交易金额: " + totalAmount);

                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // 支付成功
                    boolean result = orderService.payCallback(outTradeNo, tradeNo, totalAmount);
                    if (result) {
                        System.out.println("支付宝异步回调 - 处理成功");
                        return "success";
                    } else {
                        System.out.println("支付宝异步回调 - 处理失败");
                    }
                }
            } else {
                System.out.println("支付宝异步回调 - 签名验证失败");
            }
        } catch (AlipayApiException e) {
            System.out.println("支付宝异步回调 - 异常: " + e.getMessage());
            e.printStackTrace();
        }

        return "fail";
    }

    /**
     * 支付宝支付同步回调
     */
    @GetMapping("/return")
    public void alipayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 获取支付宝GET过来反馈信息
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        try {
            // 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayUtil.getAlipayPublicKey(),
                    "UTF-8",
                    "RSA2");

            if (signVerified) {
                // 验证成功
                // 商户订单号
                String outTradeNo = params.get("out_trade_no");
                // 支付宝交易号
                String tradeNo = params.get("trade_no");
                // 交易金额
                String totalAmount = params.get("total_amount");

                // 记录回调参数，方便调试
                System.out.println("支付宝同步回调 - 订单号: " + outTradeNo);
                System.out.println("支付宝同步回调 - 交易号: " + tradeNo);
                System.out.println("支付宝同步回调 - 交易金额: " + totalAmount);

                // 尝试更新订单状态（同步回调可能在异步回调之前到达）
                try {
                    orderService.payCallback(outTradeNo, tradeNo, totalAmount);
                } catch (Exception e) {
                    System.out.println("支付宝同步回调 - 更新订单状态失败: " + e.getMessage());
                    // 这里不需要处理异常，因为异步回调会再次尝试更新订单状态
                }

                // // 支付成功，跳转到支付结果页面
                response.sendRedirect("http://localhost:3000/#/payment/result?orderNo=" + outTradeNo + "&status=success");
            } else {
                // 验证失败
                System.out.println("支付宝同步回调 - 签名验证失败");
                response.sendRedirect("http://localhost:3000/#/payment/result?status=fail");
            }
        } catch (AlipayApiException e) {
            System.out.println("支付宝同步回调 - 异常: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("http://localhost:3000/#/payment/result?status=error");
        }
    }
}

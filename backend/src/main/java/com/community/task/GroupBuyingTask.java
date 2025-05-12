package com.community.task;

import com.community.service.GroupBuyingService;
import com.community.service.GroupOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 拼团活动定时任务
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupBuyingTask {

    private final GroupBuyingService groupBuyingService;
    private final GroupOrderService groupOrderService;

    /**
     * 每分钟检查一次拼团活动状态
     * 检查未开始的拼团是否到了开始时间，进行中的拼团是否到了结束时间
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkGroupBuyingStatus() {
        try {
            int count = groupBuyingService.checkAndUpdateStatus();
            if (count > 0) {
                log.info("定时任务：更新了{}个拼团活动状态", count);
            }
        } catch (Exception e) {
            log.error("定时任务：检查拼团活动状态异常", e);
        }
    }

    /**
     * 每5分钟检查一次过期的拼团订单
     * 检查是否有过期的拼团订单，如果有则解散
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void checkExpiredGroupOrders() {
        try {
            int count = groupOrderService.checkAndUpdateExpiredOrders();
            if (count > 0) {
                log.info("定时任务：解散了{}个过期的拼团订单", count);
            }
        } catch (Exception e) {
            log.error("定时任务：检查过期拼团订单异常", e);
        }
    }
}

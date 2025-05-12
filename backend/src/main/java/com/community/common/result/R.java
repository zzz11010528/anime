package com.community.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果类
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    public static <T> R<T> ok() {
        return restResult(null, ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    public static <T> R<T> ok(T data, String message) {
        return restResult(data, ResultCode.SUCCESS.getCode(), message);
    }

    public static <T> R<T> failed() {
        return restResult(null, ResultCode.FAILED.getCode(), ResultCode.FAILED.getMessage());
    }

    public static <T> R<T> failed(String message) {
        return restResult(null, ResultCode.FAILED.getCode(), message);
    }

    public static <T> R<T> failed(IResultCode resultCode) {
        return restResult(null, resultCode.getCode(), resultCode.getMessage());
    }

    public static <T> R<T> failed(IResultCode resultCode, String message) {
        return restResult(null, resultCode.getCode(), message);
    }

    private static <T> R<T> restResult(T data, Integer code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMessage(message);
        return r;
    }
}

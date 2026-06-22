package com.mixc.smartconcierge.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {

    private int code;
    private T data;
    private String msg;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(200, data, "success");
    }

    public static <T> ApiResult<T> ok() {
        return ok(null);
    }

    public static <T> ApiResult<T> fail(String msg) {
        return new ApiResult<>(500, null, msg);
    }

    public static <T> ApiResult<T> fail(int code, String msg) {
        return new ApiResult<>(code, null, msg);
    }
}

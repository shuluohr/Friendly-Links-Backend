package com.yupi.yupaoBackend.exception;

import com.yupi.yupaoBackend.common.ErrorCode;

/**
 * 异常类
 *
 * @author 陈君哲
 */
public class BusinessException extends RuntimeException{

    private final int code;

    private final String description;


    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}

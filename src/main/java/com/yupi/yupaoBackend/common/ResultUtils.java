package com.yupi.yupaoBackend.common;

/**
 * 返回工具类
 *
 * @author 陈君哲
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> success(ErrorCode errorCode,T data){
        return new BaseResponse<>(errorCode.getCode(), data,errorCode.getMsg());
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     * @param
     */
    public static  BaseResponse error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * @param code
     * @param msg
     * @param description
     * @return
     */
    public static BaseResponse error(int code, String msg, String description){
        return new BaseResponse<>(code,null,msg, description);
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     * @param
     */
    public static BaseResponse error(ErrorCode errorCode, String msg, String description){
        return new BaseResponse<>(errorCode.getCode(),null,msg, description);
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     * @param
     */
    public static BaseResponse error(ErrorCode errorCode, String description){
        return new BaseResponse<>(errorCode.getCode(), null,null,description);
    }
}

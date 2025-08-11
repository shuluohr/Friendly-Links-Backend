package com.yupi.yupaoBackend.model.request;

import lombok.Data;

/**
 * @author 陈君哲
 */
@Data
public class ChatRequest {

    /**
     * 房间id
     */
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 消息
     */
    private String message;
}

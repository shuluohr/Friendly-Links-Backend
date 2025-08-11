package com.yupi.yupaoBackend.model.dto;

import lombok.Data;

/**
 * @author 陈君哲
 */
@Data
public class ChatDTO {

    /**
     * 房间id
     */
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 消息
     */
    private String message;
}

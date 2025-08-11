package com.yupi.yupaoBackend.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 陈君哲
 */
@Data
@Accessors(chain = true)
public class ChatVO {
    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 消息
     */
    private String message;
}

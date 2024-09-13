package com.yupi.yupaoBackend.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author 陈君哲
 */
@Data
public class TeamUpdateRequest implements Serializable {


    @Serial
    private static final long serialVersionUID = 6734627195997371719L;

    /**
     * 队伍 id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;


    /**
     * 过期时间
     */
    private Date expireTime;


    /**
     * status 0 - 公开，1 - 私有， 2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;


}

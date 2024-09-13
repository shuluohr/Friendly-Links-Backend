package com.yupi.yupaoBackend.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author 陈君哲
 */
@Data
public class TeamJoinRequest implements Serializable {


    @Serial
    private static final long serialVersionUID = -8245908131517930309L;

    /**
     * 队伍 id
     */
    private Long teamId;


    /**
     * 密码
     */
    private String password;


}

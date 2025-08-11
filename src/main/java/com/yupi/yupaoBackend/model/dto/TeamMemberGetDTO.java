package com.yupi.yupaoBackend.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 陈君哲
 */
@Data
public class TeamMemberGetDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7943793345259310614L;

    /**
     * 队伍 id
     */
    private Long teamId;

    /**
     * 最大人数
     */
    private Integer maxNum;
}

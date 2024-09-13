package com.yupi.yupaoBackend.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author 陈君哲
 */
@Data
public class TeamQuitRequest implements Serializable {


    @Serial
    private static final long serialVersionUID = -6160840292760185840L;
    /**
     * 队伍 id
     */
    private Long teamId;

}

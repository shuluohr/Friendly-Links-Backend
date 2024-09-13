package com.yupi.yupaoBackend.model.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.yupi.yupaoBackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 队伍表
 * @TableName team
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest implements Serializable{
    /**
     * id
     */
    private Long id;

    /**
     * 搜索关键词（同时对队伍名称和描述搜索）
     */
    private String searchText;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;


    /**
     * 创建人 id
     */
    private Long userId;

    /**
     * status 0 - 公开，1 - 私有， 2 - 加密
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
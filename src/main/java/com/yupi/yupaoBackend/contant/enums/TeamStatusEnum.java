package com.yupi.yupaoBackend.contant.enums;

/**
 *队伍状态枚举
 *
 * @author 陈君哲
 */
public enum TeamStatusEnum {

    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 描述
     */
    private final String text;

    TeamStatusEnum(Integer status, String text) {
        this.status = status;
        this.text = text;
    }

    public static TeamStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : values) {
            if (teamStatusEnum.getStatus() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getText() {
        return text;
    }
}

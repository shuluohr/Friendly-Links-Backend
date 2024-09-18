package com.yupi.yupaoBackend.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用的删除请求参数
 *
 * @author 陈君哲
 */
@Data
public class DeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -6073385038977728455L;

    private long id;
}

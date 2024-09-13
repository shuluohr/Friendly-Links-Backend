package com.yupi.yupaoBackend.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用分页请求对象
 *
 * @author 陈君哲
 */

@Data
public class PageRequest  implements Serializable {


    @Serial
    private static final long serialVersionUID = 6752146499580734804L;
    /**
     * 当前第几页
     */
    protected long pageNum = 1;

    /**
     * 每页多少数据
     */
    protected long pageSize = 10;

}

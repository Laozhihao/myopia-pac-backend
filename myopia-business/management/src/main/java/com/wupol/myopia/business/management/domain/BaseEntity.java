package com.wupol.myopia.business.management.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * 基类,带页码
 * @Author Chikong
 * @Date 2020/12/22
 **/
public class BaseEntity<T> implements Serializable {
    private static final long serialVersionUID = 5454155825314635342L;

    /** 页码 */
    @JsonIgnore
    public Integer page;
    /** 条数 */
    @JsonIgnore
    public Integer size;

    /** 获取查询的页面条件 */
    public Page<T> getQueryPage() {
        return new Page<T>(page, size);
    }
}

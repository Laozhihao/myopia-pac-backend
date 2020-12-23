package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 学校列表请求体
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolListRequest {

    /**
     * 学校名称
     */
    private String name;

    /**
     * 根据规则创建ID
     */
    private Long schoolNo;

    /**
     * 学校类型
     */
    private Integer type;

    /**
     * 市/区代码
     */
    private Integer code;

    /**
     * 页码
     */
    private Integer current;

    /**
     * 页数
     */
    private Integer size;
}

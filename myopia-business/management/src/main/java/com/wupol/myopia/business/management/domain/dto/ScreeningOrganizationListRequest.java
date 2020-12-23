package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 筛查机构列表请求体
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScreeningOrganizationListRequest {

    /**
     * 根据规则创建ID
     */
    private Long orgNo;

    /**
     * 筛查机构名称
     */
    private String name;

    /**
     * 筛查机构类型
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

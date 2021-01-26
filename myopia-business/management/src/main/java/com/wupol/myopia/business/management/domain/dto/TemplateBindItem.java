package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 区域详情
 *
 * @author Simple4H
 */
@Setter
@Getter
public class TemplateBindItem {

    /**
     * 行政ID
     */
    private Integer districtId;

    /**
     * 行政名称
     */
    private String districtName;
}

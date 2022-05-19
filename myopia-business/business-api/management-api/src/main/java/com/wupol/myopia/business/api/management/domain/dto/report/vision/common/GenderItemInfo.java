package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.Getter;
import lombok.Setter;

/**
 * 散光概览描述
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GenderItemInfo {

    /**
     * 名称描述
     */
    private String name;

    /**
     * 百分比
     */
    private String proportion;

    /**
     * 男
     */
    private String mProportion;

    /**
     * 女
     */
    private String fProportion;
}

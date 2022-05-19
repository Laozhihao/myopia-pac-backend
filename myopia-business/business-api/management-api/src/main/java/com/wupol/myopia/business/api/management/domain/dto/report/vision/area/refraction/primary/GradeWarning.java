package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import lombok.Getter;
import lombok.Setter;

/**
 * 预警等级
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GradeWarning {

    /**
     * 预警率最高班级
     */
    private String maxClassNameWarning;

    /**
     * 预警率最高班级 百分比
     */
    private String maxClassWarningPercentage;

    /**
     * 预警率最低班级
     */
    private String minClassNameWarning;

    /**
     * 预警率最低班级 百分比
     */
    private String minClassWarningPercentage;
}

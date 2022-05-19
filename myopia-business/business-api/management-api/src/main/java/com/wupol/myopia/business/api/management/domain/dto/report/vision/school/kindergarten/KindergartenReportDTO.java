package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.SchoolReportInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * 视力筛查-幼儿园报告
 *
 * @author Simple4H
 */
@Getter
@Setter
public class KindergartenReportDTO {

    /**
     * 标题信息
     */
    private SchoolReportInfo info;

    /**
     * 概述
     */
    private KindergartenSchoolOutline outline;

    /**
     * 视力总体情况
     */
    private GeneralVision generalVision;

    /**
     * 各班筛查数据
     */
    private ClassScreeningData classScreeningData;
}



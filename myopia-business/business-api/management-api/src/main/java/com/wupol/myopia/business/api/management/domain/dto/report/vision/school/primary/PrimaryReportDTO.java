package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.SchoolReportInfo;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.ClassScreeningData;
import lombok.Getter;
import lombok.Setter;

/**
 * 视力筛查-小学及以上报告
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PrimaryReportDTO {

    /**
     * 标题信息
     */
    private SchoolReportInfo info;

    /**
     * 概述
     */
    private PrimarySchoolOutline outline;

    /**
     * 视力总体情况
     */
    private PrimaryGeneralVision primaryGeneralVision;

    /**
     * 各班筛查数据
     */
    private ClassScreeningData classScreeningData;
}



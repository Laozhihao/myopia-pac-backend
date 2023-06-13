package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.myopia.MyopiaSituation;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.overview.Overview;
import lombok.Data;

/**
 * 计划报告
 *
 * @author Simple4H
 */
@Data
public class PlanReportResponseDTO {

    /**
     * 概述
     */
    private Overview overview;

    /**
     * 近视情况
     */
    private MyopiaSituation myopiaSituation;

    /**
     * 视力情况
     */
    private Object visionSituation;

    /**
     * 矫正情况
     */
    private Object correctSituation;

    /**
     * 屈光情况
     */
    private Object refractionSituation;
}

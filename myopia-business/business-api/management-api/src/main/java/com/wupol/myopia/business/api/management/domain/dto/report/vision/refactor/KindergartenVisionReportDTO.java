package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten.KindergartenRefractiveSituationDTO;
import lombok.Data;

/**
 * 幼儿园视力报告
 * @Author wulizhou
 * @Date 2023/1/3 11:27
 */
@Data
public class KindergartenVisionReportDTO {

    /**
     * 概述
     */
    private Object summary;

    /**
     * 学生视力情况
     */
    private Object studentVision;

    /**
     * 屈光情况
     */
    private KindergartenRefractiveSituationDTO kindergartenRefractiveSituationDTO;

    /**
     * 预警等级
     */
    private WarningSituationDTO warningSituation;

}

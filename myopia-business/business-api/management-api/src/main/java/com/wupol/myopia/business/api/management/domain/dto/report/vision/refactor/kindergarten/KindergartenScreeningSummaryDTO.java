package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.kindergarten;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor.ReportBaseSummaryDTO;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 幼儿园筛查报告概述
 * @Author wulizhou
 * @Date 2023/1/3 18:05
 */
@Data
@Accessors(chain = true)
public class KindergartenScreeningSummaryDTO extends ReportBaseSummaryDTO {

    /**
     * 屈光不正人数
     */
    private Integer refractiveErrorNum;

    /**
     * 屈光不正率
     */
    private Float refractiveErrorRatio;

    /**
     * 屈光参差人数
     */
    private Integer anisometropiaNum;

    /**
     * 屈光参差率
     */
    private Float anisometropiaRatio;

    /**
     * 远视储备不足人数
     */
    private Integer insufficientHyperopiaNum;

    /**
     * 远视储备不足率
     */
    private Float insufficientHyperopiaRatio;

}

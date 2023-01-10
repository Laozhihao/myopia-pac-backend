package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 学校筛查概述
 *
 * @Author wulizhou
 * @Date 2022/12/23 18:07
 */
@Data
@Accessors(chain = true)
public class ScreeningSummaryDTO extends ReportBaseSummaryDTO {

    /**
     * 近视人数
     */
    private Integer myopiaNum;

    /**
     * 近视率
     */
    private Float myopiaRatio;

    /**
     * 未矫率
     */
    private Float uncorrectedRatio;

    /**
     * 欠矫率
     */
    private Float underCorrectedRatio;

    /**
     * 低度近视率
     */
    private Float lightMyopiaRatio;

    /**
     * 高度近视率
     */
    private Float highMyopiaRatio;

}

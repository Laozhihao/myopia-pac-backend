package com.wupol.myopia.business.api.management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 视力筛查报告
 * @Author wulizhou
 * @Date 2022/12/23 18:01
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class VisualScreeningReportDTO {

    /**
     * 概述
     */
    private ScreeningSummaryDTO summary;

    /**
     * 学生近视情况
     */
    private MyopiaInfoDTO studentMyopia;

    /**
     * 学生视力情况
     */
    private VisionInfoDTO studentVision;

}

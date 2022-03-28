package com.wupol.myopia.business.api.parent.domain.dto;

import lombok.Data;

/**
 * 条件获取筛查记录
 *
 * @author Simple4H
 */
@Data
public class ScreeningReportInfoResponseDTO {

    /**
     * 学生Id
     */
    private Integer studentId;

    /**
     * 报告Id
     */
    private Integer reportId;
}

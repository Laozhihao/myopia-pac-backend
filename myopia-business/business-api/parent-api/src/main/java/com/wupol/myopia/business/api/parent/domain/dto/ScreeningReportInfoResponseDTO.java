package com.wupol.myopia.business.api.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 条件获取筛查记录
 *
 * @author Simple4H
 */
@Getter
@Setter
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

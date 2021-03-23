package com.wupol.myopia.business.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 筛查报告返回体
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScreeningReportResponseDTO {

    /**
     * 详情
     */
    private ScreeningReportDetail detail;
}

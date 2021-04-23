package com.wupol.myopia.business.api.parent.domain.dto;

import com.wupol.myopia.business.api.parent.domain.dos.ScreeningReportDetailDO;
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
    private ScreeningReportDetailDO detail;
}

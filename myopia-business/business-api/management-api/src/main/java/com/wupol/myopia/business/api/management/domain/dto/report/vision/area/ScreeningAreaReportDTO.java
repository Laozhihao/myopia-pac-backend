package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import lombok.Getter;
import lombok.Setter;

/**
 * 视力筛查-筛查区域
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScreeningAreaReportDTO {

    /**
     * 标题信息
     */
    private AreaReportInfo info;

    /**
     * 概述
     */
    private AreaOutline areaOutline;

    /**
     * 视力总体情况
     */
    private AreaGeneralVision areaGeneralVision;

    /**
     * 各学校整体情况
     */
    private SchoolScreeningInfo schoolScreeningInfo;


}

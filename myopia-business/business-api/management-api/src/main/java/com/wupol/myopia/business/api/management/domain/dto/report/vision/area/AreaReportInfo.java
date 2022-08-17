package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import lombok.Getter;
import lombok.Setter;

/**
 * 视力筛查-幼儿园报告
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AreaReportInfo {

    /**
     * 区域名称
     */
    private String area;

    /**
     * 年度
     */
    private String date;
}

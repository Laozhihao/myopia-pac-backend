package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 视力筛查-幼儿园报告
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolReportInfo {

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 报告生成日期
     */
    private Date date;
}

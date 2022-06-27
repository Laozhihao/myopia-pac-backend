package com.wupol.myopia.business.api.management.domain.dto.report.vision.school;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.ScreeningDataReportTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 各班筛查数据
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ClassScreeningData {

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 表格
     */
    private List<ScreeningDataReportTable> tables;
}

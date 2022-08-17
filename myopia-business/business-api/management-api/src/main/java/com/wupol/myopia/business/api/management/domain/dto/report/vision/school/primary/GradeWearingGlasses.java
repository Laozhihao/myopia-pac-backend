package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.PortraitChart;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年级矫正戴镜情况情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GradeWearingGlasses {

    /**
     * 信息
     */
    private PrimaryWearingInfo info;

    /**
     * 戴镜类型图表
     */
    private PortraitChart gradeWearingGlassesChart;

    /**
     * 矫正类型图表
     */
    private PortraitChart gradeVisionCorrectionChart;

    /**
     * 表格
     */
    private List<AgeWearingTable> tables;
}

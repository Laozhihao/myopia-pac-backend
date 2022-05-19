package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.VisionWarningSituation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 近视预警情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class WarningSituation {

    /**
     * 近视预警情况
     */
    private VisionWarningSituation visionWarningSituation;

    /**
     * 建议就诊
     */
    private CountAndProportion recommendDoctor;

    /**
     * 预警等级
     */
    private List<GradeWarning> gradeWarning;

    /**
     * 表格
     */
    private List<Table> tables;

    /**
     * 表格
     */
    @Getter
    @Setter
    public static class Table {

        /**
         * 年级名称
         */
        private String name;

        /**
         * 有效人数
         */
        private Integer validCount;

        /**
         * 0级预警人数
         */
        private Integer zeroWarningCount;

        /**
         * 0级预警人数百分比
         */
        private String zeroWarningPercentage;

        /**
         * 1级预警人数
         */
        private Integer oneWarningCount;

        /**
         * 1级预警人数百分比
         */
        private String oneWarningPercentage;

        /**
         * 2级预警人数
         */
        private Integer twoWarningCount;

        /**
         * 2级预警人数百分比
         */
        private String twoWarningPercentage;

        /**
         * 3级预警人数
         */
        private Integer threeWarningCount;

        /**
         * 3级预警人数百分比
         */
        private String threeWarningPercentage;

        /**
         * 建议就诊人数
         */
        private Integer recommendDoctorCount;

        /**
         * 建议就诊百分比
         */
        private String recommendDoctorPercentage;
    }
}

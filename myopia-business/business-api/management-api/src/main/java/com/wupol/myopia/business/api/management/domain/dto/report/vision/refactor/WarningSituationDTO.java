package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 预警情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class WarningSituationDTO {

    /**
     * 不同年级学生视力预警情况
     */
    private GradeWarningSituation gradeWarningSituation;

    /**
     * 年级学生视力预警情况
     */
    @Getter
    @Setter
    public static class GradeWarningSituation {

        /**
         * 详情
         */
        private List<GradeWarningSituationItem> items;

        /**
         * 表格
         */
        private Object table;
    }

    /**
     * 年级学生视力预警情况
     */
    @Getter
    @Setter
    public static class GradeWarningSituationItem extends WarningSituation {

        /**
         * 年级名称
         */
        private String gradeName;
    }

    /**
     * 生视力预警情况
     */
    @Getter
    @Setter
    public static class WarningSituation {

        /**
         * 筛查学生
         */
        private Long screeningStudentNum;

        /**
         * 0级预警人数
         */
        private Long zeroWarningNum;

        /**
         * 0级预警人数百分比
         */
        private Float zeroWarningRatio;

        /**
         * 1级预警人数
         */
        private Long oneWarningNum;

        /**
         * 1级预警人数百分比
         */
        private Float oneWarningRatio;

        /**
         * 2级预警人数
         */
        private Long twoWarningNum;

        /**
         * 2级预警人数百分比
         */
        private Float twoWarningRatio;

        /**
         * 3级预警人数
         */
        private Long threeWarningNum;

        /**
         * 3级预警人数百分比
         */
        private Float threeWarningRatio;
    }

}

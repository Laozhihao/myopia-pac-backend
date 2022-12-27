package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 视力矫正情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class VisionCorrectionSituationDTO {

    /**
     * 信息
     */
    private VisionCorrectionSituationInfo visionCorrectionSituationInfo;

    /**
     * 戴镜情况
     */
    private WearingGlasses wearingGlasses;

    /**
     * 矫正情况
     */
    private CorrectionSituation correctionSituation;

    /**
     * 年级未矫/欠矫
     */
    private GradeUnderCorrectedAndUncorrected gradeUnderCorrectedAndUncorrected;

    /**
     * 班级未矫/欠矫
     */
    private ClassUnderCorrectedAndUncorrected classUnderCorrectedAndUncorrected;

    /**
     * 视力矫正情况
     */
    @Getter
    @Setter
    public static class VisionCorrectionSituationInfo {

        /**
         * 筛查学生
         */
        private Long screeningStudentNum;

        /**
         * 戴镜人数
         */
        private Long wearingGlassesNum;

        /**
         * 戴镜人数百分比
         */
        private String wearingGlassesRatio;
    }

    /**
     * 戴镜情况
     */
    @Getter
    @Setter
    public static class WearingGlasses {

        /**
         * 戴镜详情
         */
        private WearingGlassesItem wearingGlassesItem;

        /**
         * 表格
         */
        private List<PieChart> table;
    }

    /**
     * 戴镜详情
     */
    @Getter
    @Setter
    public static class WearingGlassesItem {

        /**
         * 筛查学生
         */
        private Long screeningStudentNum;

        /**
         * 不戴镜人数
         */
        private Long notWearingNum;

        /**
         * 不戴镜百分比
         */
        private String notWearingRatio;

        /**
         * 框架眼镜人数
         */
        private Long frameGlassesNum;

        /**
         * 框架眼镜百分比
         */
        private String frameGlassesRatio;

        /**
         * 隐形眼镜人数
         */
        private Long contactLensNum;

        /**
         * 隐形眼镜人数百分比
         */
        private String contactLensRatio;

        /**
         * 夜戴角膜塑形镜人数
         */
        private Long orthokeratologyNum;

        /**
         * 夜戴角膜塑形镜人数百分比
         */
        private String orthokeratologyRatio;
    }

    /**
     * 矫正情况
     */
    @Getter
    @Setter
    public static class CorrectionSituation {

        /**
         * 矫正情况
         */
        private UnderCorrectedAndUncorrected underCorrectedAndUncorrected;

        /**
         * 表格
         */
        private List<PieChart> table;
    }

    /**
     * 年级矫正情况
     */
    @Getter
    @Setter
    public static class GradeUnderCorrectedAndUncorrected {

        /**
         * 详情
         */
        private List<GradeUnderCorrectedAndUncorrectedItem> items;

        /**
         * 表格
         */
        private Object table;
    }

    /**
     * 年级矫正情况
     */
    @Getter
    @Setter
    public static class GradeUnderCorrectedAndUncorrectedItem extends UnderCorrectedAndUncorrected {

        /**
         * 年级名称
         */
        private String gradeName;
    }

    /**
     * 班级矫正情况
     */
    @Getter
    @Setter
    public static class ClassUnderCorrectedAndUncorrected {

        /**
         * 详情
         */
        private List<ClassUnderCorrectedAndUncorrectedItem> items;
    }

    /**
     * 班级矫正情况
     */
    @Getter
    @Setter
    public static class ClassUnderCorrectedAndUncorrectedItem extends UnderCorrectedAndUncorrected {


        /**
         * 年级名称
         */
        private String gradeName;

        /**
         * 班级名称
         */
        private String className;
    }

    /**
     * 矫正情况
     */
    @Getter
    @Setter
    public static class UnderCorrectedAndUncorrected {
        /**
         * 筛查学生
         */
        private Long screeningStudentNum;

        /**
         * 未矫人数
         */
        private Long uncorrectedNum;

        /**
         * 未矫百分比
         */
        private String uncorrectedRatio;

        /**
         * 欠矫人数
         */
        private Long underCorrectedNum;

        /**
         * 欠矫百分比
         */
        private String underCorrectedRatio;
    }
}
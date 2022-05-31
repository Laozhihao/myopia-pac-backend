package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 血压与脊柱弯曲监测实体
 *
 * @author hang.yuan 2022/5/16 18:34
 */
@Data
public class SchoolBloodPressureAndSpinalCurvatureMonitorVO {
    /**
     * 说明
     */
    private BloodPressureAndSpinalCurvatureMonitorVariableVO bloodPressureAndSpinalCurvatureMonitorVariableVO;

    /**
     * 龋齿监测 - 不同性别
     */
    private BloodPressureAndSpinalCurvatureSexVO bloodPressureAndSpinalCurvatureSexVO;

    /**
     * 龋齿监测 - 不同学龄段
     */
    private BloodPressureAndSpinalCurvatureGradeVO bloodPressureAndSpinalCurvatureGradeVO;
    /**
     * 龋齿监测 - 不同年龄
     */
    private BloodPressureAndSpinalCurvatureAgeVO bloodPressureAndSpinalCurvatureAgeVO;

    @Data
    public static class BloodPressureAndSpinalCurvatureMonitorVariableVO {
        /**
         * 血压偏高率
         */
        private String highBloodPressureRatio;

        /**
         * 脊柱弯曲异常率
         */
        private String abnormalSpineCurvatureRatio;
    }



    @Data
    public static class BloodPressureAndSpinalCurvatureSexVO{
        /**
         * 性别说明
         */
        private BloodPressureAndSpinalCurvatureSexVariableVO bloodPressureAndSpinalCurvatureSexVariableVO;
        /**
         * 性别表格数据
         */
        private List<BloodPressureAndSpinalCurvatureMonitorTable> bloodPressureAndSpinalCurvatureSexMonitorTableList;


    }

    @Data
    public static class BloodPressureAndSpinalCurvatureSexVariableVO{
        /**
         * 血压偏高率对比
         */
        private BloodPressureAndSpinalCurvatureSex highBloodPressureRatioCompare;
        /**
         * 脊柱弯曲异常率对比
         */
        private BloodPressureAndSpinalCurvatureSex abnormalSpineCurvatureRatioCompare;

    }

    @Data
    public static class BloodPressureAndSpinalCurvatureSex{
        /**
         * 前：性别
         */
        private String forwardSex;
        /**
         * 前：占比
         */
        private String forwardRatio;
        /**
         * 后：性别
         */
        private String backSex;
        /**
         * 后：占比
         */
        private String backRatio;
    }


    @Data
    public static class BloodPressureAndSpinalCurvatureGradeVO{

        /**
         * 年级说明
         */
        private BloodPressureAndSpinalCurvatureGradeVariableVO bloodPressureAndSpinalCurvatureGradeVariableVO;
        /**
         * 年级表格数据
         */
        private List<BloodPressureAndSpinalCurvatureMonitorTable> bloodPressureAndSpinalCurvatureGradeMonitorTableList;

    }
    @Data
    public static class BloodPressureAndSpinalCurvatureGradeVariableVO{
        /**
         * 最高年级血压偏高率
         */
        private GradeRatio highBloodPressureRatio;

        /**
         * 最高年级脊柱弯曲异常率
         */
        private GradeRatio abnormalSpineCurvatureRatio;
    }


    @Data
    public static class GradeRatio{
        /**
         * 最高占比年级
         */
        private String maxGrade;
        /**
         * 最低占比年级
         */
        private String minGrade;
        /**
         * 最高占比
         */
        private String maxRatio;
        /**
         * 最低占比
         */
        private String minRatio;
    }


    @Data
    public static class BloodPressureAndSpinalCurvatureAgeVO{
        /**
         * 年龄段说明
         */
        private BloodPressureAndSpinalCurvatureAgeVariableVO bloodPressureAndSpinalCurvatureAgeVariableVO;
        /**
         * 年龄段数据
         */
        private List<BloodPressureAndSpinalCurvatureMonitorTable> bloodPressureAndSpinalCurvatureAgeMonitorTableList;

    }

    @Data
    public static class BloodPressureAndSpinalCurvatureAgeVariableVO{

        /**
         * 血压偏高率 最高、最低
         */
        private AgeRatio highBloodPressureRatio;
        /**
         * 最高年级血压偏高率 最高、最低
         */
        private AgeRatio abnormalSpineCurvatureRatio;

    }

    @Data
    public static class AgeRatio{
        /**
         * 最高占比年龄
         */
        private String maxAge;
        /**
         * 最低占比年龄
         */
        private String minAge;
        /**
         * 最高占比
         */
        private String maxRatio;
        /**
         * 最低占比
         */
        private String minRatio;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class BloodPressureAndSpinalCurvatureMonitorTable extends SchoolCommonDiseasesAnalysisVO.BloodPressureAndSpinalCurvatureVO{

        /**
         * 项目 （性别、学龄段、年龄段）
         */
        private String itemName;

        /**
         * 筛查人数(有效数据)
         */
        private Integer validScreeningNum;

    }
}

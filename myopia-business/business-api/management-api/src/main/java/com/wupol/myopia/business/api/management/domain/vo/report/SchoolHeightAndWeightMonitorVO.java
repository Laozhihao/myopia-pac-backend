package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 身高体重监测实体
 *
 * @author hang.yuan 2022/5/16 18:31
 */
@Data
public class SchoolHeightAndWeightMonitorVO {

    /**
     * 说明变量
     */
    private HeightAndWeightMonitorVariableVO heightAndWeightMonitorVariableVO;

    /**
     * 龋齿监测 - 不同性别
     */
    private HeightAndWeightSexVO heightAndWeightSex;

    /**
     * 龋齿监测 - 不同学龄段
     */
    private HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO;
    /**
     * 龋齿监测 - 不同年龄
     */
    private HeightAndWeightAgeVO heightAndWeightAgeVO;

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class HeightAndWeightMonitorVariableVO extends DistrictCommonDiseasesAnalysisVO.HeightAndWeightRatioVO{

    }



    @Data
    public static class HeightAndWeightSexVO{
        /**
         * 性别说明
         */
        private HeightAndWeightSexVariableVO heightAndWeightSexVariableVO;
        /**
         * 性别数据
         */
        private List<HeightAndWeightMonitorTable> heightAndWeightMonitorTableList;


    }

    @Data
    public static class HeightAndWeightSexVariableVO{
        /**
         * 超重率对比
         */
        private HeightAndWeightSex overweightRatioCompare;
        /**
         * 肥胖率对比
         */
        private HeightAndWeightSex obeseRatioCompare;
        /**
         * 营养不良率对比
         */
        private HeightAndWeightSex malnourishedRatioCompare;
        /**
         * 生长迟缓对比
         */
        private HeightAndWeightSex stuntingRatioCompare;
    }

    @Data
    public static class HeightAndWeightSex{
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
    public static class HeightAndWeightSchoolAgeVO{

        /**
         * 学龄段说明
         */
        private HeightAndWeightSchoolAgeVariableVO heightAndWeightSchoolAgeVariableVO;
        /**
         * 学龄段数据
         */
        private List<HeightAndWeightMonitorTable> heightAndWeightMonitorTableList;

    }
    @Data
    public static class HeightAndWeightSchoolAgeVariableVO{
        /**
         * 小学
         */
        private HeightAndWeightSchoolAge primarySchool;
        /**
         * 初中
         */
        private HeightAndWeightSchoolAge juniorHighSchool;
        /**
         * 普高
         */
        private HeightAndWeightSchoolAge normalHighSchool;
        /**
         * 职高
         */
        private HeightAndWeightSchoolAge vocationalHighSchool;
        /**
         * 大学
         */
        private HeightAndWeightSchoolAge university;

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class HeightAndWeightSchoolAge extends DistrictCommonDiseasesAnalysisVO.HeightAndWeightRatioVO{

        /**
         * 最高年级超重率
         */
        private GradeRatio maxOverweightRatio;
        /**
         * 最高年级肥胖率
         */
        private GradeRatio maxObeseRatio;

        /**
         * 最高年级营养不良率
         */
        private GradeRatio maxMalnourishedRatio;

        /**
         * 最高年级生长迟缓率
         */
        private GradeRatio maxStuntingRatio;

    }

    @Data
    public static class GradeRatio{
        /**
         * 年级
         */
        private String grade;
        /**
         * 占比
         */
        private String ratio;
    }


    @Data
    public static class HeightAndWeightAgeVO{
        /**
         * 年龄段说明
         */
        private HeightAndWeightAgeVariableVO heightAndWeightAgeVariableVO;
        /**
         * 年龄段数据
         */
        private List<HeightAndWeightMonitorTable> heightAndWeightMonitorTableList;

    }

    @Data
    public static class HeightAndWeightAgeVariableVO{
        /**
         * 超重率 最高、最低
         */
        private AgeRatio overweightRatio;
        /**
         * 肥胖率 最高、最低
         */
        private AgeRatio obeseRatio;
        /**
         * 营养不良率 最高、最低
         */
        private AgeRatio malnourishedRatio;
        /**
         * 生长迟缓率 最高、最低
         */
        private AgeRatio stuntingRatio;

    }

    @Data
    public static class AgeRatio{
        /**
         * 年龄
         */
        private String grade;
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
    public static class HeightAndWeightMonitorTable extends DistrictCommonDiseasesAnalysisVO.HeightAndWeightVO{

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

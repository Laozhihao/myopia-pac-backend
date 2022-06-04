package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 身高体重监测实体
 *
 * @author hang.yuan 2022/5/16 18:31
 */
@Data
public class DistrictHeightAndWeightMonitorVO {

    /**
     * 说明
     */
    private HeightAndWeightMonitorVariableVO heightAndWeightMonitorVariableVO;

    /**
     * 龋齿监测 - 不同性别
     */
    private HeightAndWeightSexVO heightAndWeightSexVO;

    /**
     * 龋齿监测 - 不同学龄段
     */
    private HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO;
    /**
     * 龋齿监测 - 不同年龄
     */
    private HeightAndWeightAgeVO heightAndWeightAgeVO;

    @Data
    public static class HeightAndWeightMonitorVariableVO {
        /**
         * 超重率
         */
        private String overweightRatio;
        /**
         * 肥胖率
         */
        private String obeseRatio;

        /**
         * 营养不良率
         */
        private String malnourishedRatio;

        /**
         * 生长迟缓率
         */
        private String stuntingRatio;

    }


    @Data
    public static class HeightAndWeightSexVO{
        /**
         * 性别说明
         */
        private HeightAndWeightSexVariableVO heightAndWeightSexVariableVO;
        /**
         * 性别表格数据
         */
        private List<HeightAndWeightMonitorTable> heightAndWeightSexMonitorTableList;
        /**
         * 性别图表
         */
        private DistrictChartVO.Chart heightAndWeightSexMonitorChart;


    }

    @Data
    public static class HeightAndWeightSexVariableVO{
        /**
         * 超重率对比
         */
        private SexCompare overweightRatioCompare;
        /**
         * 肥胖率对比
         */
        private SexCompare obeseRatioCompare;
        /**
         * 营养不良率对比
         */
        private SexCompare malnourishedRatioCompare;
        /**
         * 生长迟缓对比
         */
        private SexCompare stuntingRatioCompare;
    }



    @Data
    public static class HeightAndWeightSchoolAgeVO{

        /**
         * 学龄段说明
         */
        private HeightAndWeightSchoolAgeVariableVO heightAndWeightSchoolAgeVariableVO;
        /**
         * 学龄段表格数据
         */
        private List<HeightAndWeightMonitorTable> heightAndWeightSchoolAgeMonitorTableList;
        /**
         * 学龄段图表
         */
        private DistrictChartVO.Chart heightAndWeightSchoolAgeMonitorChart;

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
         * 高中（普高+职高）
         */
        private HeightAndWeightSchoolAge highSchool;
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

    @Data
    public static class HeightAndWeightSchoolAge {

        /**
         * 超重率
         */
        private String overweightRatio;
        /**
         * 肥胖率
         */
        private String obeseRatio;

        /**
         * 营养不良率
         */
        private String malnourishedRatio;

        /**
         * 生长迟缓率
         */
        private String stuntingRatio;

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

        public GradeRatio(String grade, String ratio) {
            this.grade = grade;
            this.ratio = ratio;
        }

        public GradeRatio() {
        }
    }


    @Data
    public static class HeightAndWeightAgeVO{
        /**
         * 年龄段说明
         */
        private HeightAndWeightAgeVariableVO heightAndWeightAgeVariableVO;
        /**
         * 年龄段表格数据
         */
        private List<HeightAndWeightMonitorTable> heightAndWeightAgeMonitorTableList;
        /**
         * 年龄段图表
         */
        private DistrictChartVO.AgeChart heightAndWeightAgeMonitorChart;

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
         * 最高年龄段
         */
        private String maxAge;
        /**
         * 最低年龄段
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

}

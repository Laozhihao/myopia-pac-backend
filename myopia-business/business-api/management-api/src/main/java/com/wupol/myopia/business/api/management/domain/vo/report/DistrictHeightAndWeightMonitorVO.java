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
     * 身高体重监测 - 不同性别
     */
    private HeightAndWeightSexVO heightAndWeightSexVO;

    /**
     * 身高体重监测 - 不同学龄段
     */
    private HeightAndWeightSchoolAgeVO heightAndWeightSchoolAgeVO;
    /**
     * 身高体重监测 - 不同年龄
     */
    private HeightAndWeightAgeVO heightAndWeightAgeVO;



    @Data
    public static class HeightAndWeightSchoolAgeVO {

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
        private ChartVO.Chart heightAndWeightSchoolAgeMonitorChart;

    }

    @Data
    public static class HeightAndWeightSchoolAgeVariableVO {
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
    public static class GradeRatio {
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

}

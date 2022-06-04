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
    private HeightAndWeightGradeVO heightAndWeightGradeVO;
    /**
     * 龋齿监测 - 不同年龄
     */
    private HeightAndWeightAgeVO heightAndWeightAgeVO;



    @Data
    public static class HeightAndWeightGradeVO{

        /**
         * 年级说明
         */
        private HeightAndWeightGradeVariableVO heightAndWeightGradeVariableVO;
        /**
         * 年级表格数据
         */
        private List<HeightAndWeightMonitorTable> heightAndWeightGradeMonitorTableList;
        /**
         * 学龄段图表
         */
        private ChartVO.Chart heightAndWeightGradeMonitorChart;

    }
    @Data
    public static class HeightAndWeightGradeVariableVO{
        /**
         * 最高年级超重率
         */
        private GradeRatio overweightRatio;
        /**
         * 最高年级肥胖率
         */
        private GradeRatio obeseRatio;

        /**
         * 最高年级营养不良率
         */
        private GradeRatio malnourishedRatio;

        /**
         * 最高年级生长迟缓率
         */
        private GradeRatio stuntingRatio;
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


}

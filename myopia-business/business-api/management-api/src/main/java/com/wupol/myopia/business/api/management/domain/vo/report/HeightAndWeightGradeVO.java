package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 身高体重监测-不同班级
 *
 * @author hang.yuan
 * @date 2022/6/8
 */
@Data
public class HeightAndWeightGradeVO implements GradeChartVO {

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

    @Override
    public Integer type() {
        return 2;
    }


    @Data
    public static class HeightAndWeightGradeVariableVO {
        /**
         * 最高年级超重率
         */
        private SchoolGradeRatio overweightRatio;
        /**
         * 最高年级肥胖率
         */
        private SchoolGradeRatio obeseRatio;

        /**
         * 最高年级营养不良率
         */
        private SchoolGradeRatio malnourishedRatio;

        /**
         * 最高年级生长迟缓率
         */
        private SchoolGradeRatio stuntingRatio;
    }


}
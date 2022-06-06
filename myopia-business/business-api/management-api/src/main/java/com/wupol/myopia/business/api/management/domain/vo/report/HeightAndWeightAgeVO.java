package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

@Data
public class HeightAndWeightAgeVO implements AgeChartVO {
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
    private ChartVO.AgeChart heightAndWeightAgeMonitorChart;

    @Override
    public Integer type() {
        return 2;
    }

    @Data
    public static class HeightAndWeightAgeVariableVO {
        /**
         * 超重率 最高、最低
         */
        private AgeRatioVO overweightRatio;
        /**
         * 肥胖率 最高、最低
         */
        private AgeRatioVO obeseRatio;
        /**
         * 营养不良率 最高、最低
         */
        private AgeRatioVO malnourishedRatio;
        /**
         * 生长迟缓率 最高、最低
         */
        private AgeRatioVO stuntingRatio;

    }

}
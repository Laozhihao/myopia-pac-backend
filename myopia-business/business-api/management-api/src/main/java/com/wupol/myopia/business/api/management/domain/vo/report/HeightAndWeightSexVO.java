package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 身高体重监测-不同性别
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class HeightAndWeightSexVO implements SexChartVO {
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
    private ChartVO.Chart heightAndWeightSexMonitorChart;

    @Override
    public Integer type() {
        return 2;
    }


    @Data
    public static class HeightAndWeightSexVariableVO {
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


}
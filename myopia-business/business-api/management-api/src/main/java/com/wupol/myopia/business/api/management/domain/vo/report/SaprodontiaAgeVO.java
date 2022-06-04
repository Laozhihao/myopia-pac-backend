package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

@Data
public class SaprodontiaAgeVO implements AgeChartVO {
    /**
     * 年龄段说明
     */
    private SaprodontiaAgeVariableVO saprodontiaAgeVariableVO;
    /**
     * 年龄段表格数据
     */
    private List<SaprodontiaMonitorTable> saprodontiaAgeMonitorTableList;
    /**
     * 年龄段图表
     */
    private ChartVO.AgeChart saprodontiaAgeMonitorChart;

    @Override
    public Integer getType() {
        return 2;
    }


    @Data
    public static class SaprodontiaAgeVariableVO {
        /**
         * 龋患率 最高、最低
         */
        private AgeRatio saprodontiaRatio;
        /**
         * 龋失率 最高、最低
         */
        private AgeRatio saprodontiaLossRatio;
        /**
         * 龋补率 最高、最低
         */
        private AgeRatio saprodontiaRepairRatio;

    }

    @Data
    public static class AgeRatio{
        /**
         * 最高年龄段
         */
        private String maxAge;
        /**
         * 最小年龄段
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
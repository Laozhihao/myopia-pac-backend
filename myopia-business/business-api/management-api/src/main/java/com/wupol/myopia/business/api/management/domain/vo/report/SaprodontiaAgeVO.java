package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 龋齿监测-不同年龄段
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
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
    private ChartVO.Chart saprodontiaAgeMonitorChart;

    @Override
    public Integer type() {
        return 1;
    }


    @Data
    public static class SaprodontiaAgeVariableVO {
        /**
         * 龋患率 最高、最低
         */
        private AgeRatioVO saprodontiaRatio;
        /**
         * 龋失率 最高、最低
         */
        private AgeRatioVO saprodontiaLossRatio;
        /**
         * 龋补率 最高、最低
         */
        private AgeRatioVO saprodontiaRepairRatio;

    }

}
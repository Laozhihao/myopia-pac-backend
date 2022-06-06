package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 龋齿监测-不同性别
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class SaprodontiaSexVO implements SexChartVO {
    /**
     * 性别说明
     */
    private SaprodontiaSexVariableVO saprodontiaSexVariableVO;
    /**
     * 性别表格数据
     */
    private List<SaprodontiaMonitorTable> saprodontiaSexMonitorTableList;
    /**
     * 性别图表
     */
    private ChartVO.Chart saprodontiaSexMonitorChart;

    @Override
    public Integer type() {
        return 1;
    }


    @Data
    public static class SaprodontiaSexVariableVO {
        /**
         * 龋患率对比
         */
        private SexCompare saprodontiaRatioCompare;
        /**
         * 龋失率对比
         */
        private SexCompare saprodontiaLossRatioCompare;
        /**
         * 龋补率对比
         */
        private SexCompare saprodontiaRepairRatioCompare;
    }

}
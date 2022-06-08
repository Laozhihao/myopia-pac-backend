package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 龋齿监测-不同班级
 *
 * @author hang.yuan
 * @date 2022/6/8
 */
@Data
public class SaprodontiaGradeVO implements GradeChartVO {

    /**
     * 年级说明
     */
    private SaprodontiaGradeVariableVO saprodontiaGradeVariableVO;
    /**
     * 年级表格数据
     */
    private List<SaprodontiaMonitorTable> saprodontiaGradeMonitorTableList;
    /**
     * 年级图表
     */
    private ChartVO.Chart saprodontiaGradeMonitorChart;

    @Override
    public Integer type() {
        return 1;
    }


    @Data
    public static class SaprodontiaGradeVariableVO {
        /**
         * 最高年级龋患率
         */
        private SchoolGradeRatio saprodontiaRatio;

        /**
         * 最高年级龋失率
         */
        private SchoolGradeRatio saprodontiaLossRatio;

        /**
         * 最高年级龋补率
         */
        private SchoolGradeRatio saprodontiaRepairRatio;

    }


}
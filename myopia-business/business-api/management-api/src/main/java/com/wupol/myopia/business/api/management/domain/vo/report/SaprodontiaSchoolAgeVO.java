package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 龋齿监测-不同学龄
 * @author hang.yuan
 * @date 2022/6/8
 */
@Data
public class SaprodontiaSchoolAgeVO implements SchoolAgeChartVO {

    /**
     * 学龄段说明
     */
    private SaprodontiaSchoolAgeVariableVO saprodontiaSchoolAgeVariableVO;
    /**
     * 学龄段表格数据
     */
    private List<SaprodontiaMonitorTable> saprodontiaSchoolAgeMonitorTableList;
    /**
     * 学龄段图表
     */
    private ChartVO.Chart saprodontiaSchoolAgeMonitorChart;

    @Override
    public Integer type() {
        return 1;
    }

    @Data
    public static class SaprodontiaSchoolAgeVariableVO {
        /**
         * 小学
         */
        private SaprodontiaSchoolAge primarySchool;
        /**
         * 初中
         */
        private SaprodontiaSchoolAge juniorHighSchool;

        /**
         * 高中(普高+职高)
         */
        private SaprodontiaSchoolAge highSchool;
        /**
         * 普高
         */
        private SaprodontiaSchoolAge normalHighSchool;
        /**
         * 职高
         */
        private SaprodontiaSchoolAge vocationalHighSchool;
        /**
         * 大学
         */
        private SaprodontiaSchoolAge university;

    }
}
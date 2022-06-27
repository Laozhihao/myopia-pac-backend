package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 身高体重监测-不同学龄
 *
 * @author hang.yuan
 * @date 2022/6/8
 */
@Data
public class HeightAndWeightSchoolAgeVO implements SchoolAgeChartVO {

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

    @Override
    public Integer type() {
        return 2;
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
}
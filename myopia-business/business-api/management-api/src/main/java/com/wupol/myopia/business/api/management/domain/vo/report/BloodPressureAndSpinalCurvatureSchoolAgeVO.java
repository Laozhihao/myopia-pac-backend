package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 血压与脊柱弯曲异常-不同学龄
 *
 * @author hang.yuan
 * @date 2022/6/8
 */
@Data
public class BloodPressureAndSpinalCurvatureSchoolAgeVO implements SchoolAgeChartVO {

    /**
     * 学龄段说明
     */
    private BloodPressureAndSpinalCurvatureSchoolAgeVariableVO bloodPressureAndSpinalCurvatureSchoolAgeVariableVO;
    /**
     * 学龄段表格数据
     */
    private List<BloodPressureAndSpinalCurvatureMonitorTable> bloodPressureAndSpinalCurvatureSchoolAgeMonitorTableList;
    /**
     * 学龄段图表
     */
    private ChartVO.Chart bloodPressureAndSpinalCurvatureSchoolAgeMonitorChart;

    @Override
    public Integer type() {
        return 3;
    }


    @Data
    public static class BloodPressureAndSpinalCurvatureSchoolAgeVariableVO{
        /**
         * 小学
         */
        private BloodPressureAndSpinalCurvatureSchoolAge primarySchool;
        /**
         * 初中
         */
        private BloodPressureAndSpinalCurvatureSchoolAge juniorHighSchool;
        /**
         * 高中（普高+职高）
         */
        private BloodPressureAndSpinalCurvatureSchoolAge highSchool;
        /**
         * 普高
         */
        private BloodPressureAndSpinalCurvatureSchoolAge normalHighSchool;
        /**
         * 职高
         */
        private BloodPressureAndSpinalCurvatureSchoolAge vocationalHighSchool;
        /**
         * 大学
         */
        private BloodPressureAndSpinalCurvatureSchoolAge university;

    }


}
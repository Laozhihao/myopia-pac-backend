package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 血压与脊柱弯曲异常监测 - 不同性别
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class BloodPressureAndSpinalCurvatureSexVO implements SexChartVO {
    /**
     * 性别说明
     */
    private BloodPressureAndSpinalCurvatureSexVariableVO bloodPressureAndSpinalCurvatureSexVariableVO;
    /**
     * 性别表格数据
     */
    private List<BloodPressureAndSpinalCurvatureMonitorTable> bloodPressureAndSpinalCurvatureSexMonitorTableList;
    /**
     * 性别图表
     */
    private ChartVO.Chart bloodPressureAndSpinalCurvatureSexMonitorChart;

    @Override
    public Integer type() {
        return 3;
    }


    @Data
    public static class BloodPressureAndSpinalCurvatureSexVariableVO {
        /**
         * 血压偏高率对比
         */
        private SexCompare highBloodPressureRatioCompare;
        /**
         * 脊柱弯曲异常率对比
         */
        private SexCompare abnormalSpineCurvatureRatioCompare;

    }

}
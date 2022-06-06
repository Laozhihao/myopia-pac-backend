package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 血压与脊柱弯曲异常监测 - 不同年龄段
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class BloodPressureAndSpinalCurvatureAgeVO implements AgeChartVO {
    /**
     * 年龄段说明
     */
    private BloodPressureAndSpinalCurvatureAgeVariableVO bloodPressureAndSpinalCurvatureAgeVariableVO;
    /**
     * 年龄段表格数据
     */
    private List<BloodPressureAndSpinalCurvatureMonitorTable> bloodPressureAndSpinalCurvatureAgeMonitorTableList;
    /**
     * 年龄段图表
     */
    private ChartVO.AgeChart bloodPressureAndSpinalCurvatureAgeMonitorChart;

    @Override
    public Integer type() {
        return 3;
    }


    @Data
    public static class BloodPressureAndSpinalCurvatureAgeVariableVO {

        /**
         * 血压偏高率 最高、最低
         */
        private AgeRatioVO highBloodPressureRatio;
        /**
         * 脊柱弯曲异常率 最高、最低
         */
        private AgeRatioVO abnormalSpineCurvatureRatio;

    }
}
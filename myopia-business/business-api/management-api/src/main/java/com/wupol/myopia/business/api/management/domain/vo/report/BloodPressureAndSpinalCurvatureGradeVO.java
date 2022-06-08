package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

import java.util.List;

/**
 * 血压与脊柱弯曲异常-不同班级
 *
 * @author hang.yuan
 * @date 2022/6/8
 */
@Data
public class BloodPressureAndSpinalCurvatureGradeVO implements GradeChartVO {

    /**
     * 年级说明
     */
    private BloodPressureAndSpinalCurvatureGradeVariableVO bloodPressureAndSpinalCurvatureGradeVariableVO;
    /**
     * 年级表格数据
     */
    private List<BloodPressureAndSpinalCurvatureMonitorTable> bloodPressureAndSpinalCurvatureGradeMonitorTableList;
    /**
     * 年级图表
     */
    private ChartVO.Chart bloodPressureAndSpinalCurvatureGradeMonitorChart;

    @Override
    public Integer type() {
        return 3;
    }


    @Data
    public static class BloodPressureAndSpinalCurvatureGradeVariableVO {
        /**
         * 最高年级血压偏高率
         */
        private SchoolGradeRatio highBloodPressureRatio;

        /**
         * 最高年级脊柱弯曲异常率
         */
        private SchoolGradeRatio abnormalSpineCurvatureRatio;
    }


}
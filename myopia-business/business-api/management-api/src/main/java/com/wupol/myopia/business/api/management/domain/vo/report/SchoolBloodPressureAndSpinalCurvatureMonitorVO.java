package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 血压与脊柱弯曲监测实体
 *
 * @author hang.yuan 2022/5/16 18:34
 */
@Data
public class SchoolBloodPressureAndSpinalCurvatureMonitorVO {
    /**
     * 说明
     */
    private BloodPressureAndSpinalCurvatureMonitorVariableVO bloodPressureAndSpinalCurvatureMonitorVariableVO;

    /**
     * 血压与脊柱弯曲异常监测 - 不同性别
     */
    private BloodPressureAndSpinalCurvatureSexVO bloodPressureAndSpinalCurvatureSexVO;

    /**
     * 血压与脊柱弯曲异常监测 - 不同年级
     */
    private BloodPressureAndSpinalCurvatureGradeVO bloodPressureAndSpinalCurvatureGradeVO;
    /**
     * 血压与脊柱弯曲异常监测 - 不同年龄段
     */
    private BloodPressureAndSpinalCurvatureAgeVO bloodPressureAndSpinalCurvatureAgeVO;



    @Data
    public static class BloodPressureAndSpinalCurvatureGradeVO{

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

    }
    @Data
    public static class BloodPressureAndSpinalCurvatureGradeVariableVO{
        /**
         * 最高年级血压偏高率
         */
        private GradeRatio highBloodPressureRatio;

        /**
         * 最高年级脊柱弯曲异常率
         */
        private GradeRatio abnormalSpineCurvatureRatio;
    }


    @Data
    public static class GradeRatio{
        /**
         * 最高占比年级
         */
        private String maxGrade;
        /**
         * 最低占比年级
         */
        private String minGrade;
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

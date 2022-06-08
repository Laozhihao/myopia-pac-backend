package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 血压与脊柱弯曲监测实体
 *
 * @author hang.yuan 2022/5/16 18:34
 */
@Data
public class DistrictBloodPressureAndSpinalCurvatureMonitorVO {
    /**
     * 说明
     */
    private BloodPressureAndSpinalCurvatureMonitorVariableVO bloodPressureAndSpinalCurvatureMonitorVariableVO;

    /**
     * 龋齿监测 - 不同性别
     */
    private BloodPressureAndSpinalCurvatureSexVO bloodPressureAndSpinalCurvatureSexVO;

    /**
     * 龋齿监测 - 不同学龄段
     */
    private BloodPressureAndSpinalCurvatureSchoolAgeVO bloodPressureAndSpinalCurvatureSchoolAgeVO;
    /**
     * 龋齿监测 - 不同年龄
     */
    private BloodPressureAndSpinalCurvatureAgeVO bloodPressureAndSpinalCurvatureAgeVO;



    @Data
    public static class BloodPressureAndSpinalCurvatureSchoolAgeVO{

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

    @Data
    public static class BloodPressureAndSpinalCurvatureSchoolAge {

        /**
         * 血压偏高率
         */
        private String highBloodPressureRatio;

        /**
         * 脊柱弯曲异常率
         */
        private String abnormalSpineCurvatureRatio;
        /**
         * 最高年级血压偏高率
         */
        private GradeRatio maxHighBloodPressureRatio;

        /**
         * 最高年级脊柱弯曲异常率
         */
        private GradeRatio maxAbnormalSpineCurvatureRatio;


    }

    @Data
    public static class GradeRatio{
        /**
         * 年级
         */
        private String grade;
        /**
         * 占比
         */
        private String ratio;

    }

}

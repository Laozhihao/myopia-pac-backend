package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

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



}

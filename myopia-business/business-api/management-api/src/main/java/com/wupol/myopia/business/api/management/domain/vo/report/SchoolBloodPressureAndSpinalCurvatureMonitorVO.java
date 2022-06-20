package com.wupol.myopia.business.api.management.domain.vo.report;

import lombok.Data;

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


}

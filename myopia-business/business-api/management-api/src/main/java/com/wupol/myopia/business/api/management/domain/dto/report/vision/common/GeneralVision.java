package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 视力总体情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GeneralVision {

    /**
     * 视力情况
     */
    private VisionSituation visionSituation;

    /**
     * 不同性别视力低下
     */
    private SexLowVision sexLowVision;

    /**
     * 不同班级视力低下
     */
    private GradeLowVision gradeLowVision;

    /**
     * 屈光情况
     */
    private RefractiveAbnormalities refractiveAbnormalities;

    /**
     * 不同性别屈光
     */
    private SexRefractive sexRefractive;

    /**
     * 不同班级屈光
     */
    private GradeRefractive gradeRefractive;

    /**
     * 视力预警情况
     */
    private VisionWarningSituation visionWarningSituation;

    /**
     * 建议就诊
     */
    private CountAndProportion recommendDoctor;

    /**
     * 不同年级班级视力预警情况
     */
    private GradeWarning gradeWarning;
}

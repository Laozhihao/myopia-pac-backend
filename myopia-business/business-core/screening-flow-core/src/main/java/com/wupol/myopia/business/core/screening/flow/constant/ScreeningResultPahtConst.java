package com.wupol.myopia.business.core.screening.flow.constant;

import lombok.experimental.UtilityClass;

/**
 * 模板路径
 *
 * @author Alix
 */
@UtilityClass
public class ScreeningResultPahtConst {
    /** 右眼裸眼视力 */
    public final String RIGHTEYE_NAKED_VISION = "$.visionData.rightEyeData.nakedVision";
    /** 左眼裸眼视力 */
    public final String LEFTEYE_NAKED_VISION = "$.visionData.leftEyeData.nakedVision";
    /** 右眼矫正视力 */
    public final String RIGHTEYE_CORRECTED_VISION = "$.visionData.rightEyeData.correctedVision";
    /** 左眼矫正视力 */
    public final String LEFTEYE_CORRECTED_VISION = "$.visionData.leftEyeData.correctedVision";
    /** 右眼球镜 */
    public final String RIGHTEYE_SPH = "$.computerOptometry.rightEyeData.sph";
    /** 左眼球镜 */
    public final String LEFTEYE_SPH = "$.computerOptometry.leftEyeData.sph";
    /** 右眼柱镜 */
    public final String RIGHTEYE_CYL = "$.computerOptometry.rightEyeData.cyl";
    /** 左眼柱镜 */
    public final String LEFTEYE_CYL = "$.computerOptometry.leftEyeData.cyl";
    /** 右眼轴位 */
    public final String RIGHTEYE_AXIAL = "$.computerOptometry.rightEyeData.axial";
    /** 左眼轴位 */
    public final String LEFTEYE_AXIAL = "$.computerOptometry.leftEyeData.axial";
}

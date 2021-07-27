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

    // 33cm眼位
    public final String PATH_OID_ESOTROPIA = "$.ocularInspectionData.esotropia";
    public final String PATH_OID_EXOTROPIA = "$.ocularInspectionData.exotropia";
    public final String PATH_OID_VERTICAL_STRABISMUS = "$.ocularInspectionData.verticalStrabismus";
    public final String PATH_OID_DIAGNOSIS = "$.ocularInspectionData.diagnosis";

    // 视光检测
    public final String PATH_VD_DIAGNOSIS = "$.visionData.diagnosis";

    // 电脑验光
    public final String PATH_CO_diagnosis = "$.computerOptometry.diagnosis";
//    public final String result = "$.computerOptometry.leftEyeData.axial";

    // 裂隙灯检查
    public final String PATH_SLD_RIGHT_PATHOLOGICAL_TISSUES = "$.slitLampData.rightEyeData.pathologicalTissues";
    public final String PATH_SLD_RIGHT_DIAGNOSIS = "$.slitLampData.rightEyeData.diagnosis";
    public final String PATH_SLD_LEFT_PATHOLOGICAL_TISSUES = "$.slitLampData.leftEyeData.pathologicalTissues";
    public final String PATH_SLD_LEFT_DIAGNOSIS = "$.slitLampData.leftEyeData.diagnosis";

    // 小瞳验光
    public final String PATH_POD_RIGHT_AXIAL = "$.pupilOptometryData.rightEyeData.axial";
    public final String PATH_POD_RIGHT_SPN = "$.pupilOptometryData.rightEyeData.sph";
    public final String PATH_POD_RIGHT_CYL = "$.pupilOptometryData.rightEyeData.cyl";
    public final String PATH_POD_RIGHT_CORRECTEDVISION = "$.pupilOptometryData.rightEyeData.correctedVision";
    public final String PATH_POD_LEFT_AXIAL = "$.pupilOptometryData.leftEyeData.axial";
    public final String PATH_POD_LEFT_SPN = "$.pupilOptometryData.leftEyeData.sph";
    public final String PATH_POD_LEFT_CYL = "$.pupilOptometryData.leftEyeData.cyl";
    public final String PATH_POD_LEFT_CORRECTEDVISION = "$.pupilOptometryData.rightEyeData.correctedVision";
    public final String PATH_POD_DIAGNOSIS = "$.pupilOptometryData.diagnosis";
//    public final String result = "$.pupilOptometryData.leftEyeData.correctedVision";

    // 生物测量
    public final String PATH_BD_LEFT_AD = "$.biometricData.leftEyeData.ad";
    public final String PATH_BD_LEFT_AL = "$.biometricData.leftEyeData.al";
    public final String PATH_BD_LEFT_CCT = "$.biometricData.leftEyeData.cct";
    public final String PATH_BD_LEFT_LT = "$.biometricData.leftEyeData.lt";
    public final String PATH_BD_LEFT_WTW = "$.biometricData.leftEyeData.wtw";
    public final String PATH_BD_LEFT_K1 = "$.biometricData.leftEyeData.k1";
    public final String PATH_BD_LEFT_K2 = "$.biometricData.leftEyeData.k2";
    public final String PATH_BD_LEFT_AST = "$.biometricData.leftEyeData.ast";
    public final String PATH_BD_LEFT_PD = "$.biometricData.leftEyeData.pd";
    public final String PATH_BD_LEFT_VT = "$.biometricData.leftEyeData.vt";
    public final String PATH_BD_RIGHT_AD = "$.biometricData.rightEyeData.ad";
    public final String PATH_BD_RIGHT_AL = "$.biometricData.rightEyeData.al";
    public final String PATH_BD_RIGHT_CCT = "$.biometricData.rightEyeData.cct";
    public final String PATH_BD_RIGHT_LT = "$.biometricData.rightEyeData.lt";
    public final String PATH_BD_RIGHT_WTW = "$.biometricData.rightEyeData.wtw";
    public final String PATH_BD_RIGHT_K1 = "$.biometricData.rightEyeData.k1";
    public final String PATH_BD_RIGHT_K2 = "$.biometricData.rightEyeData.k2";
    public final String PATH_BD_RIGHT_AST = "$.biometricData.rightEyeData.ast";
    public final String PATH_BD_RIGHT_PD = "$.biometricData.rightEyeData.pd";
    public final String PATH_BD_RIGHT_VT = "$.biometricData.rightEyeData.vt";

    // 眼压
    public final String PATH_IPD_RIGHT_PRESSURE = "$.intraocularPressureData.rightEyeData.pressure";
    public final String PATH_IPD_LEFT_PRESSURE = "$.intraocularPressureData.leftEyeData.pressure";

    // 眼底
    public final String PATH_DF_RIGHT_HASABNORMAL= "$.fundusData.rightEyeData.hasAbnormal";
    public final String PATH_DF_LEFT_HASABNORMAL = "$.fundusData.leftEyeData.hasAbnormal";

    // 其他眼病
    public final String PATH_OED_LEFT_EYE_DISEASES = "$.otherEyeDiseases.leftEyeData.eyeDiseases";
    public final String PATH_OED_RIGHT_EYE_DISEASES = "$.otherEyeDiseases.rightEyeData.eyeDiseases";
    public final String PATH_SYSTEMIC_DISEASE_SYMPTOM = "$.systemicDiseaseSymptom";
    public final String PATH_VLLD_RIGHT_LEVEL = "$.visualLossLevelData.rightEyeData.level";
    public final String PATH_VLLD_LEFT_LEVEL = "$.visualLossLevelData.leftEyeData.level";

}

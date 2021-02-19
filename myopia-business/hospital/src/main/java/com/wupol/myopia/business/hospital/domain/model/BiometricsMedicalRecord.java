package com.wupol.myopia.business.hospital.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 生物测量检查数据
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class BiometricsMedicalRecord {
    /** 学生id */
    private Integer studentId;
    /** 1散瞳前，2散瞳后 */
    private Integer checkType;
    /** 右眼轴位 */
    private String rightAxis;
    /** 左眼轴位 */
    private String leftAxis;
    /** 右眼前房深度 */
    private String rightDepthPreviousChamber;
    /** 左眼前房深度 */
    private String leftDepthPreviousChamber;
    /** 右眼中央房水深度 */
    private String rightAqueousDepth;
    /** 左眼中央房水深度 */
    private String leftAqueousDepth;
    /** 右眼晶状体厚度 */
    private String rightLensThickness;
    /** 左眼晶状体厚度 */
    private String leftLensThickness;
    /** 右眼瞳孔直径 */
    private String rightPupilDiameter;
    /** 左眼瞳孔直径 */
    private String leftPupilDiameter;
    /** 右眼角膜中央厚度 */
    private String rightCentral;
    /** 左眼角膜中央厚度 */
    private String leftCentral;
    /** 右眼角膜白到白 */
    private String rightWhiteToWhite;
    /** 左眼角膜白到白 */
    private String leftWhiteToWhite;
    /** 右眼角膜K1 */
    private String rightSimK1Radius;
    /** 右眼角膜K1 */
    private String rightSimK1Axis;
    /** 左眼角膜K1 */
    private String leftSimK1Radius;
    /** 左眼角膜K1 */
    private String leftSimK1Axis;
    /** 右眼角膜K2 */
    private String rightSimK2Radius;
    /** 右眼角膜K2 */
    private String rightSimK2Axis;
    /** 左眼角膜K2 */
    private String leftSimK2Radius;
    /** 左眼角膜K2 */
    private String leftSimK2Axis;
    /** 备注 */
    private String remark;
    /** 影像列表 */
    private List<String> imageList;

}

package com.wupol.myopia.business.core.hospital.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
    /** 散瞳前 */
    private Biometrics nonMydriasis;
    /** 散瞳后 */
    private Biometrics mydriasis;

    @Getter
    @Setter
    @Accessors(chain = true)
    public class Biometrics {
        /** 1散瞳前，2散瞳后 */
        private Integer checkType;
        /** 右眼轴位 */
        private String rightAxis;
        /** 左眼轴位 */
        private String leftAxis;
        /** 右眼前房深度 */
        private String rightACD;
        /** 左眼前房深度 */
        private String leftACD;
        /** 右眼中央房水深度 */
        private String rightAD;
        /** 左眼中央房水深度 */
        private String leftAD;
        /** 右眼晶状体厚度 */
        private String rightLT;
        /** 左眼晶状体厚度 */
        private String leftLT;
        /** 右眼瞳孔直径 */
        private String rightPD;
        /** 左眼瞳孔直径 */
        private String leftPD;
        /** 右眼角膜中央厚度 */
        private String rightCCT;
        /** 左眼角膜中央厚度 */
        private String leftCCT;
        /** 右眼角膜白到白 */
        private String rightWTW;
        /** 左眼角膜白到白 */
        private String leftWTW;
        /** 右眼角膜K1 */
        private String rightK1Radius;
        /** 右眼角膜K1 */
        private String rightK1Axis;
        /** 左眼角膜K1 */
        private String leftK1Radius;
        /** 左眼角膜K1 */
        private String leftK1Axis;
        /** 右眼角膜K2 */
        private String rightK2Radius;
        /** 右眼角膜K2 */
        private String rightK2Axis;
        /** 左眼角膜K2 */
        private String leftK2Radius;
        /** 左眼角膜K2 */
        private String leftK2Axis;
        /** 右眼垂直方向角膜散光度数 */
        private String rightAST;
        /** 左眼垂直方向角膜散光度数 */
        private String leftAST;
        /** 右眼晶状体厚度 */
        private String rightVT;
        /** 左眼晶状体厚度 */
        private String leftVT;
        /** 备注 */
        private String remark;

    }

}

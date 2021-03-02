package com.wupol.myopia.business.hospital.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 屈光检查数据
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
public class DiopterMedicalRecord {
    /** 学生id */
    private Integer studentId;
    /** 散瞳前 */
    private Diopter nonMydriasis;
    /** 散瞳后 */
    private Diopter mydriasis;


    @Getter
    @Setter
    @Accessors(chain = true)
    public class Diopter {
        /** 1散瞳前，2散瞳后 */
        private Integer checkType;
        // 电脑验光
        /** 右眼球镜 */
        private String computerRightSphere;
        /** 左眼球镜 */
        private String computerLeftSphere;
        /** 右眼柱镜 */
        private String computerRightCylinder;
        /** 左眼柱镜 */
        private String computerLeftCylinder;
        /** 右眼轴位 */
        private String computerRightAxis;
        /** 左眼轴位 */
        private String computerLeftAxis;
        /** 电脑瞳距 */
        private String computerPupilDistance;

        // 检影验光
        /** 右眼球镜 */
        private String retinoscopyesRightSphere;
        /** 左眼球镜 */
        private String retinoscopyesLeftSphere;
        /** 右眼柱镜 */
        private String retinoscopyesRightCylinder;
        /** 左眼柱镜 */
        private String retinoscopyesLeftCylinder;
        /** 右眼轴位 */
        private String retinoscopyesRightAxis;
        /** 左眼轴位 */
        private String retinoscopyesLeftAxis;
        /** 右眼视力 */
        private String retinoscopyesRightVision;
        /** 左眼视力 */
        private String retinoscopyesLeftVision;
        /** 检影瞳距 */
        private String retinoscopyesPupilDistance;
        /** 主导眼。1右眼，2左眼。非散瞳验光必填 */
        private String mainEye;
        /** 双眼平衡法。非散瞳验光必填。 */
        private String eyeBalance;

        /** 备注 */
        private String remark;

    }

}

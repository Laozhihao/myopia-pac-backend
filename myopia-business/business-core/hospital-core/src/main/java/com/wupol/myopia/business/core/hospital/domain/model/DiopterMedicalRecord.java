package com.wupol.myopia.business.core.hospital.domain.model;

import com.wupol.myopia.business.core.hospital.domain.interfaces.HasResult;
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

    private Integer doctorId;
    /** 学生id */
    private Integer studentId;
    /** 散瞳前 */
    private Diopter nonMydriasis;
    /** 散瞳后 */
    private Diopter mydriasis;


    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Diopter implements HasResult {
        /** 1散瞳前，2散瞳后 */
        private Integer checkType;
        // 电脑验光
        /** 右眼球镜 */
        private String computerRightDS;
        /** 左眼球镜 */
        private String computerLeftDS;
        /** 右眼柱镜 */
        private String computerRightDC;
        /** 左眼柱镜 */
        private String computerLeftDC;
        /** 右眼轴位 */
        private String computerRightAxis;
        /** 左眼轴位 */
        private String computerLeftAxis;
        /** 右眼轴位 */
        private String computerRightSE;
        /** 左眼轴位 */
        private String computerLeftSE;
        /** 电脑瞳距 */
        private String computerPD;

        // 检影验光
        /** 右眼球镜 */
        private String retinoscopyRightDS;
        /** 左眼球镜 */
        private String retinoscopyLeftDS;
        /** 右眼柱镜 */
        private String retinoscopyRightDC;
        /** 左眼柱镜 */
        private String retinoscopyLeftDC;
        /** 右眼轴位 */
        private String retinoscopyRightAxis;
        /** 左眼轴位 */
        private String retinoscopyLeftAxis;
        /** 右眼视力 */
        private String retinoscopyRightVision;
        /** 左眼视力 */
        private String retinoscopyLeftVision;
        /** 检影瞳距 */
        private String retinoscopyPD;
        /** 主导眼。1右眼，2左眼。非散瞳验光必填 */
        private String mainEye;
        /** 双眼平衡法。非散瞳验光必填。 */
        private String eyeBalance;
        /** 药品 */
        private Integer drug;
        /** 备注 */
        private String remark;
        /** 右眼状态 */
        private List<Integer> computerRightStatus;
        /** 左眼状态 */
        private List<Integer> computerLeftStatus;

        private Integer doctorId;
        /** 学生id */
        private Integer studentId;

        private Boolean isAbnormal;
        /** 结论 */
        private String conclusion;

    }

}

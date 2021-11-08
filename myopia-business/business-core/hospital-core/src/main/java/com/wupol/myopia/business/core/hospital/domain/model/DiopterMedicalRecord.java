package com.wupol.myopia.business.core.hospital.domain.model;

import com.wupol.framework.core.util.ObjectsUtil;
import com.wupol.framework.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        /** 右眼等效球镜SE */
        private String computerRightSE;
        /** 左眼等效球镜SE */
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

        public String getComputerRightSE() {
            if (ObjectsUtil.allNotNull(computerRightDS, computerRightDC)) {
                return new BigDecimal(computerRightDS).add(new BigDecimal(computerRightDC).multiply(new BigDecimal("0.5")))
                        .setScale(2, RoundingMode.HALF_UP).toString();
            }
            return StringUtils.EMPTY;
        }

        public String getComputerLeftSE() {
            if (ObjectsUtil.allNotNull(computerLeftDS, computerLeftDC)) {
                return new BigDecimal(computerLeftDS).add(new BigDecimal(computerLeftDC).multiply(new BigDecimal("0.5")))
                        .setScale(2, RoundingMode.HALF_UP).toString();
            }
            return StringUtils.EMPTY;
        }
    }

}

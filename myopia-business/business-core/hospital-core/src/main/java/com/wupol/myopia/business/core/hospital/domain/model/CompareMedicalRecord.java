package com.wupol.myopia.business.core.hospital.domain.model;

import com.wupol.framework.core.util.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * 对比检查单
 * @author Chikong
 * @date 2021-05-19
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CompareMedicalRecord extends MedicalRecord{

    /** 对比的屈光检查 */
    private DiopterMedicalRecord compareDiopter;
    /** 对比的生物测量 */
    private BiometricsMedicalRecord compareBiometrics;
    /** 数据对比日期记录 */
    private List<MedicalRecordDate> compareDateList;

    /** 右眼等效球镜SE */
    private String nonMydriasisComputerRightSE;
    /** 左眼等效球镜SE */
    private String nonMydriasisComputerLeftSE;

    /** 右眼等效球镜SE */
    private String mydriasisComputerRightSE;
    /** 左眼等效球镜SE */
    private String mydriasisComputerLeftSE;


    public String getNonMydriasisComputerRightSE() {
        if (Objects.isNull(compareDiopter)) {
            return StringUtils.EMPTY;
        }
        DiopterMedicalRecord.Diopter diopter = compareDiopter.getNonMydriasis();
        return getSE(diopter.getComputerRightDS(), diopter.getComputerRightDC());
    }

    public String getNonMydriasisComputerLeftSE() {
        if (Objects.isNull(compareDiopter)) {
            return StringUtils.EMPTY;
        }
        DiopterMedicalRecord.Diopter diopter = compareDiopter.getNonMydriasis();
        return getSE(diopter.getComputerLeftDS(), diopter.getComputerLeftDC());
    }

    public String getMydriasisComputerRightSE() {
        if (Objects.isNull(compareDiopter)) {
            return StringUtils.EMPTY;
        }
        DiopterMedicalRecord.Diopter diopter = compareDiopter.getMydriasis();
        return getSE(diopter.getComputerRightDS(), diopter.getComputerRightDC());
    }

    public String getMydriasisComputerLeftSE() {
        if (Objects.isNull(compareDiopter)) {
            return StringUtils.EMPTY;
        }
        DiopterMedicalRecord.Diopter mydriasis = compareDiopter.getMydriasis();
        return getSE(mydriasis.getComputerLeftDS(), mydriasis.getComputerLeftDC());
    }


    private String getSE(String val1, String val2) {
        if (!StringUtils.allHasLength(val1, val2)) {
            return StringUtils.EMPTY;
        }
        return new BigDecimal(val1).add(new BigDecimal(val2).multiply(new BigDecimal("0.5")))
                .setScale(2, RoundingMode.HALF_UP).toString();
    }

}

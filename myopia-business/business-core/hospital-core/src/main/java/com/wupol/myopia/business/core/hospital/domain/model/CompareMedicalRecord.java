package com.wupol.myopia.business.core.hospital.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

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

}

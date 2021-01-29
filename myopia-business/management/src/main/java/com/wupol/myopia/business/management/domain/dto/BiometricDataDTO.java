package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.dos.BiometricDataDO;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import lombok.Data;

/**
 * @Description
 * @Date 2021/1/26 1:08
 * @Author by Jacob
 */
@Data
public class BiometricDataDTO extends ScreeningResultBasicData {

    /**
     * lAD
     */
    private String lAD;
    /**
     * rAD
     */
    private String rAD;
    /**
     * lAL
     */
    private String lAL;
    /**
     * rAL
     */
    private String rAL;
    /**
     * rCCT
     */
    private String rCCT;
    /**
     * lCCT
     */
    private String lCCT;
    /**
     * lLT
     */
    private String lLT;
    /**
     * rlt
     */
    private String rLT;
    /**
     * lWTW
     */
    private String lWTW;
    /**
     * rWTW
     */
    private String rWTW;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        BiometricDataDO.BiometricData leftBiometricData = new BiometricDataDO.BiometricData().setAD(lAD).setAL(lAL).setCCT(lCCT).setLT(lLT).setLateriality(0);
        BiometricDataDO.BiometricData rightBiometricData = new BiometricDataDO.BiometricData().setAD(rAD).setAL(rAL).setCCT(rCCT).setLT(rLT).setLateriality(1);
        BiometricDataDO biometricDataDO = new BiometricDataDO().setRightEyeData(rightBiometricData).setLeftEyeData(leftBiometricData);
        return visionScreeningResult.setBiometricData(biometricDataDO);
    }
}

package com.wupol.myopia.business.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("l_AD")
    private String lAD;
    /**
     * rAD
     */
    @JsonProperty("r_AD")
    private String rAD;
    /**
     * lAL
     */
    @JsonProperty("l_AL")
    private String lAL;
    /**
     * rAL
     */
    @JsonProperty("r_AL")
    private String rAL;
    /**
     * rCCT
     */
    @JsonProperty("r_CCT")
    private String rCCT;
    /**
     * lCCT
     */
    @JsonProperty("l_CCT")
    private String lCCT;
    /**
     * lLT
     */
    @JsonProperty("l_LT")
    private String lLT;
    /**
     * rlt
     */
    @JsonProperty("r_LT")
    private String rLT;
    /**
     * lWTW
     */
    @JsonProperty("l_WTW")
    private String lWTW;
    /**
     * rWTW
     */
    @JsonProperty("r_WTW")
    private String rWTW;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        BiometricDataDO.BiometricData leftBiometricData = new BiometricDataDO.BiometricData().setWtw(lWTW).setAd(lAD).setAl(lAL).setCct(lCCT).setLt(lLT).setLateriality(0);
        BiometricDataDO.BiometricData rightBiometricData = new BiometricDataDO.BiometricData().setWtw(rWTW).setAd(rAD).setAl(rAL).setCct(rCCT).setLt(rLT).setLateriality(1);
        BiometricDataDO biometricDataDO = new BiometricDataDO().setRightEyeData(rightBiometricData).setLeftEyeData(leftBiometricData);
        return visionScreeningResult.setBiometricData(biometricDataDO);
    }
}

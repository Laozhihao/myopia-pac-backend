package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.business.core.screening.flow.domain.dos.BiometricDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import org.springframework.util.StringUtils;

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

    /**
     * 角膜前表面曲率K1
     */
    @JsonProperty("l_K1")
    private String lk1;
    /**
     * 角膜前表面曲率K1的度数
     */
    @JsonProperty("l_K1Axis")
    private String lk1Axis;
    /**
     * 角膜前表面曲率K2
     */
    @JsonProperty("l_K2")
    private String lk2;
    /**
     * 角膜前表面曲率K2的度数
     */
    @JsonProperty("l_K2Axis")
    private String lk2Axis;
    /**
     * 垂直方向角膜散光度数
     */
    @JsonProperty("l_AST")
    private String last;
    /**
     * 瞳孔直径
     */
    @JsonProperty("l_PD")
    private String lpd;
    /**
     * 玻璃体厚度
     */
    @JsonProperty("l_VT")
    private String lvt;

    /**
     * 角膜前表面曲率K1
     */
    @JsonProperty("r_K1")
    private String rk1;
    /**
     * 角膜前表面曲率K1的度数
     */
    @JsonProperty("r_K1Axis")
    private String rk1Axis;
    /**
     * 角膜前表面曲率K2
     */
    @JsonProperty("r_K2")
    private String rk2;
    /**
     * 角膜前表面曲率K2的度数
     */
    @JsonProperty("r_K2Axis")
    private String rk2Axis;
    /**
     * 垂直方向角膜散光度数
     */
    @JsonProperty("r_AST")
    private String rast;
    /**
     * 瞳孔直径
     */
    @JsonProperty("r_PD")
    private String rpd;
    /**
     * 玻璃体厚度
     */
    @JsonProperty("r_VT")
    private String rvt;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        BiometricDataDO.BiometricData leftBiometricData = new BiometricDataDO.BiometricData().setWtw(lWTW).setAd(lAD).setAl(lAL).setCct(lCCT).setLt(lLT).setK1(lk1).setK1Axis(lk1Axis).setK2(lk2).setK2Axis(lk2Axis).setAst(last).setPd(lpd).setVt(lvt).setLateriality(0);
        BiometricDataDO.BiometricData rightBiometricData = new BiometricDataDO.BiometricData().setWtw(rWTW).setAd(rAD).setAl(rAL).setCct(rCCT).setLt(rLT).setK1(rk1).setK1Axis(rk1Axis).setK2(rk2).setK2Axis(rk2Axis).setAst(rast).setPd(rpd).setVt(rvt).setLateriality(1);
        BiometricDataDO biometricDataDO = new BiometricDataDO().setRightEyeData(rightBiometricData).setLeftEyeData(leftBiometricData).setIsCooperative(isCooperative);
        return visionScreeningResult.setBiometricData(biometricDataDO);
    }

    public boolean isValid() {
        return StringUtils.hasText(lWTW) || StringUtils.hasText(lAD) || StringUtils.hasText(lAL) || StringUtils.hasText(lCCT) || StringUtils.hasText(lLT) || StringUtils.hasText(lk1) || StringUtils.hasText(lk1Axis) || StringUtils.hasText(lk2) || StringUtils.hasText(lk2Axis) || StringUtils.hasText(last) || StringUtils.hasText(lpd) || StringUtils.hasText(lvt)
        || StringUtils.hasText(rWTW) || StringUtils.hasText(rAD) || StringUtils.hasText(rAL) || StringUtils.hasText(rCCT) || StringUtils.hasText(rLT) || StringUtils.hasText(rk1) || StringUtils.hasText(rk1Axis) || StringUtils.hasText(rk2) || StringUtils.hasText(rk2Axis) || StringUtils.hasText(rast) || StringUtils.hasText(rpd) || StringUtils.hasText(rvt);
    }
}
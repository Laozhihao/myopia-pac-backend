package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.dos.BiometricDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

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
        BiometricDataDO.BiometricData leftBiometricData = new BiometricDataDO.BiometricData().setWtw(lWTW).setAd(lAD).setAl(lAL).setCct(lCCT).setLt(lLT).setK1(lk1).setK1Axis(lk1Axis).setK2(lk2).setK2Axis(lk2Axis).setAst(last).setPd(lpd).setVt(lvt).setLateriality(CommonConst.LEFT_EYE);
        BiometricDataDO.BiometricData rightBiometricData = new BiometricDataDO.BiometricData().setWtw(rWTW).setAd(rAD).setAl(rAL).setCct(rCCT).setLt(rLT).setK1(rk1).setK1Axis(rk1Axis).setK2(rk2).setK2Axis(rk2Axis).setAst(rast).setPd(rpd).setVt(rvt).setLateriality(CommonConst.RIGHT_EYE);
        BiometricDataDO biometricDataDO = new BiometricDataDO().setRightEyeData(rightBiometricData).setLeftEyeData(leftBiometricData).setIsCooperative(isCooperative);
        biometricDataDO.setCreateUserId(getCreateUserId());
        return visionScreeningResult.setBiometricData(biometricDataDO);
    }

    public boolean isValid() {
        return !StringUtils.isAllBlank(lWTW, lAD, lAL, lCCT, lLT, lk1, lk1Axis, lk2, lk2Axis, last, lpd, lvt,
                rWTW, rAD, rAL, rCCT, rLT, rk1, rk1Axis, rk2, rk2Axis, rast, rpd, rvt);
    }

    public static BiometricDataDTO getInstance(BiometricDataDO biometricDataDO) {
        if (Objects.isNull(biometricDataDO)) {
            return null;
        }
        BiometricDataDTO biometricDataDTO = new BiometricDataDTO();
        BiometricDataDO.BiometricData leftEye = biometricDataDO.getLeftEyeData();
        if (Objects.nonNull(leftEye)) {
            biometricDataDTO.setLAD(leftEye.getAd());
            biometricDataDTO.setLAL(leftEye.getAl());
            biometricDataDTO.setLast(leftEye.getAst());
            biometricDataDTO.setLCCT(leftEye.getCct());
            biometricDataDTO.setLk1(leftEye.getK1());
            biometricDataDTO.setLk2(leftEye.getK2());
            biometricDataDTO.setLk1Axis(leftEye.getK1Axis());
            biometricDataDTO.setLk2Axis(leftEye.getK2Axis());
            biometricDataDTO.setLLT(leftEye.getLt());
            biometricDataDTO.setLvt(leftEye.getVt());
            biometricDataDTO.setLpd(leftEye.getPd());
            biometricDataDTO.setLWTW(leftEye.getWtw());
        }
        BiometricDataDO.BiometricData rightEye = biometricDataDO.getRightEyeData();
        if (Objects.nonNull(rightEye)) {
            biometricDataDTO.setRAD(rightEye.getAd());
            biometricDataDTO.setRAL(rightEye.getAl());
            biometricDataDTO.setRast(rightEye.getAst());
            biometricDataDTO.setRCCT(rightEye.getCct());
            biometricDataDTO.setRk1(rightEye.getK1());
            biometricDataDTO.setRk2(rightEye.getK2());
            biometricDataDTO.setRk1Axis(rightEye.getK1Axis());
            biometricDataDTO.setRk2Axis(rightEye.getK2Axis());
            biometricDataDTO.setRLT(rightEye.getLt());
            biometricDataDTO.setRvt(rightEye.getVt());
            biometricDataDTO.setRpd(rightEye.getPd());
            biometricDataDTO.setRWTW(rightEye.getWtw());
        }
        biometricDataDTO.setIsCooperative(biometricDataDO.getIsCooperative());
        return biometricDataDTO;
    }
}
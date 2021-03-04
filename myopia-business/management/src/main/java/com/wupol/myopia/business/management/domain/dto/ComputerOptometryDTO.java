package com.wupol.myopia.business.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.business.management.domain.dos.BiometricDataDO;
import com.wupol.myopia.business.management.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description 电脑验光数据
 * @Date 2021/1/22 16:37
 * @Author by jacob
 */
@Data
public class ComputerOptometryDTO extends ScreeningResultBasicData{

    /**
     * 右眼轴位
     */
    @JsonProperty("r_axial")
    private String rAxial;
    /**
     * 左眼轴位
     */
    @JsonProperty("l_axial")
    private String lAxial;
    /**
     * 左眼球镜
     */
    @JsonProperty("l_sph")
    private BigDecimal lSph;
    /**
     * 右眼球镜
     */
    @JsonProperty("r_sph")
    private BigDecimal rSph;
    /**
     * 右眼柱镜
     */
    @JsonProperty("r_cyl")
    private BigDecimal rCyl;
    /**
     * 左眼柱镜
     */
    @JsonProperty("l_cyl")
    private BigDecimal lCyl;

/*

    *//**
     * 左眼串镜
     *//*
    @JsonProperty("l_lcj")
    private String lLcj;

    *//**
     * 右眼串镜
     *//*
    @JsonProperty("r_lcj")
    private String rLcj;

    *//**
     * 右眼屈光
     *//*
    @JsonProperty("r_qg")
    private String rQg;
    *//**
     * 左眼屈光
     *//*
    @JsonProperty("l_qg")
    private String  lQg;*/

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        ComputerOptometryDO.ComputerOptometry leftComputerOptometry = new ComputerOptometryDO.ComputerOptometry().setAxial(lAxial).setCyl(lCyl).setSph(lSph).setLateriality(0);
        ComputerOptometryDO.ComputerOptometry rightComputerOptometry = new ComputerOptometryDO.ComputerOptometry().setAxial(rAxial).setCyl(rCyl).setSph(rSph).setLateriality(1);
        ComputerOptometryDO computerOptometryDO = new ComputerOptometryDO().setRightEyeData(rightComputerOptometry).setLeftEyeData(leftComputerOptometry);
        return visionScreeningResult.setComputerOptometry(computerOptometryDO);
    }
}


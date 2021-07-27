package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description 电脑验光数据
 * @Date 2021/1/22 16:37
 * @Author by jacob
 */
@Data
public class ComputerOptometryDTO extends ScreeningResultBasicData {

    /**
     * 右眼轴位
     */
    @JsonProperty("r_axial")
    private BigDecimal rAxial;
    /**
     * 左眼轴位
     */
    @JsonProperty("l_axial")
    private BigDecimal lAxial;
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
    /**
     * 初步诊断结果：0-正常、1-（疑似）异常
     */
    private Integer diagnosis;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        ComputerOptometryDO.ComputerOptometry leftComputerOptometry = new ComputerOptometryDO.ComputerOptometry().setAxial(lAxial).setCyl(lCyl).setSph(lSph).setLateriality(0);
        ComputerOptometryDO.ComputerOptometry rightComputerOptometry = new ComputerOptometryDO.ComputerOptometry().setAxial(rAxial).setCyl(rCyl).setSph(rSph).setLateriality(1);
        ComputerOptometryDO computerOptometryDO = new ComputerOptometryDO().setRightEyeData(rightComputerOptometry).setLeftEyeData(leftComputerOptometry).setDiagnosis(diagnosis).setIsCooperative(isCooperative);
        return visionScreeningResult.setComputerOptometry(computerOptometryDO);
    }
}


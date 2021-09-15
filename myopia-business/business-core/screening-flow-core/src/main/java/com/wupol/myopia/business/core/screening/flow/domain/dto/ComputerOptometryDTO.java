package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.util.Objects;

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
        ComputerOptometryDO.ComputerOptometry leftComputerOptometry = new ComputerOptometryDO.ComputerOptometry().setAxial(lAxial).setCyl(lCyl).setSph(lSph).setLateriality(CommonConst.LEFT_EYE);
        ComputerOptometryDO.ComputerOptometry rightComputerOptometry = new ComputerOptometryDO.ComputerOptometry().setAxial(rAxial).setCyl(rCyl).setSph(rSph).setLateriality(CommonConst.RIGHT_EYE);
        ComputerOptometryDO computerOptometryDO = new ComputerOptometryDO().setRightEyeData(rightComputerOptometry).setLeftEyeData(leftComputerOptometry).setIsCooperative(isCooperative);
        computerOptometryDO.setDiagnosis(diagnosis);
        computerOptometryDO.setCreateUserId(getCreateUserId());
        return visionScreeningResult.setComputerOptometry(computerOptometryDO);
    }

    public boolean isValid() {
        return ObjectUtils.anyNotNull(rAxial, lAxial, lSph, rSph, rCyl, lCyl);
    }

    public static ComputerOptometryDTO getInstance(ComputerOptometryDO computerOptometryDO) {
        ComputerOptometryDTO computerOptometryDTO = new ComputerOptometryDTO();
        if (Objects.isNull(computerOptometryDO)) {
            return computerOptometryDTO;
        }
        ComputerOptometryDO.ComputerOptometry leftEye = computerOptometryDO.getLeftEyeData();
        if (Objects.nonNull(leftEye)) {
            computerOptometryDTO.setLAxial(leftEye.getAxial());
            computerOptometryDTO.setLCyl(leftEye.getCyl());
            computerOptometryDTO.setLSph(leftEye.getSph());
        }
        ComputerOptometryDO.ComputerOptometry rightEye = computerOptometryDO.getRightEyeData();
        if (Objects.nonNull(rightEye)) {
            computerOptometryDTO.setRAxial(rightEye.getAxial());
            computerOptometryDTO.setRCyl(rightEye.getCyl());
            computerOptometryDTO.setRSph(rightEye.getSph());
        }
        computerOptometryDTO.setDiagnosis(computerOptometryDO.getDiagnosis());
        computerOptometryDTO.setIsCooperative(computerOptometryDO.getIsCooperative());
        return computerOptometryDTO;
    }
}


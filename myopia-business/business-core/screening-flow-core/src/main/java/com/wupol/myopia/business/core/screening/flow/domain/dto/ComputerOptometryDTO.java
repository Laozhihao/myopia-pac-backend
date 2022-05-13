package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.base.util.BigDecimalUtil;
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
     * 左眼等效球镜
     */
    @JsonProperty("l_se")
    private BigDecimal lSe;

    /**
     * 左眼等效球镜
     */
    @JsonProperty("r_se")
    private BigDecimal rSe;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        ComputerOptometryDO.ComputerOptometry leftComputerOptometry = new ComputerOptometryDO.ComputerOptometry().setAxial(lAxial).setCyl(lCyl).setSph(lSph).setLateriality(CommonConst.LEFT_EYE);
        ComputerOptometryDO.ComputerOptometry rightComputerOptometry = new ComputerOptometryDO.ComputerOptometry().setAxial(rAxial).setCyl(rCyl).setSph(rSph).setLateriality(CommonConst.RIGHT_EYE);
        ComputerOptometryDO computerOptometryDO = new ComputerOptometryDO().setRightEyeData(rightComputerOptometry).setLeftEyeData(leftComputerOptometry).setIsCooperative(super.getIsCooperative());
        computerOptometryDO.setDiagnosis(super.getDiagnosis());
        computerOptometryDO.setCreateUserId(getCreateUserId());
        return visionScreeningResult.setComputerOptometry(computerOptometryDO);
    }

    public boolean isValid() {
        // 不配合时全部校验
        if (super.getIsCooperative() == 1) {
            return true;
        }
        return ObjectUtils.anyNotNull(rAxial, lAxial, lSph, rSph, rCyl, lCyl);
    }

    public static ComputerOptometryDTO getInstance(ComputerOptometryDO computerOptometryDO) {
        if (Objects.isNull(computerOptometryDO)) {
            return null;
        }
        ComputerOptometryDTO computerOptometryDTO = new ComputerOptometryDTO();
        ComputerOptometryDO.ComputerOptometry leftEye = computerOptometryDO.getLeftEyeData();
        if (Objects.nonNull(leftEye)) {
            computerOptometryDTO.setLAxial(leftEye.getAxial());
            computerOptometryDTO.setLCyl(leftEye.getCyl());
            computerOptometryDTO.setLSph(leftEye.getSph());
            // 等效球镜= 球镜+（1/2）柱镜
            computerOptometryDTO.setLSe(BigDecimalUtil.getBigDecimalByFormat(leftEye.getCyl().multiply(BigDecimal.valueOf(0.5)).add(leftEye.getSph()), 2));
        }
        ComputerOptometryDO.ComputerOptometry rightEye = computerOptometryDO.getRightEyeData();
        if (Objects.nonNull(rightEye)) {
            computerOptometryDTO.setRAxial(rightEye.getAxial());
            computerOptometryDTO.setRCyl(rightEye.getCyl());
            computerOptometryDTO.setRSph(rightEye.getSph());
            computerOptometryDTO.setRSe(BigDecimalUtil.getBigDecimalByFormat(rightEye.getCyl().multiply(BigDecimal.valueOf(0.5)).add(rightEye.getSph()), 2));
        }
        computerOptometryDTO.setDiagnosis(computerOptometryDO.getDiagnosis());
        computerOptometryDTO.setIsCooperative(computerOptometryDO.getIsCooperative());
        return computerOptometryDTO;
    }
}


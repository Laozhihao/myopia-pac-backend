package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dos.ComputerOptometryDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @Description 电脑验光数据
 * @Date 2021/1/22 16:37
 * @Author by jacob
 */
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
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

    /**
     * 左眼角膜曲率k1
     */
    @JsonProperty("l_k1")
    private BigDecimal lK1;

    /**
     * 左眼角膜曲率k2
     */
    @JsonProperty("l_k2")
    private BigDecimal lK2;

    /**
     * 右眼角膜曲率k1
     */
    @JsonProperty("r_k1")
    private BigDecimal rK1;

    /**
     *
     * 右眼角膜曲率k2
     */
    @JsonProperty("r_k2")
    private BigDecimal rK2;


    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        ComputerOptometryDO.ComputerOptometry leftComputerOptometry = new ComputerOptometryDO.ComputerOptometry()
                .setAxial(lAxial).setCyl(lCyl).setSph(lSph).setK1(lK1).setK2(lK2).setLateriality(CommonConst.LEFT_EYE);
        ComputerOptometryDO.ComputerOptometry rightComputerOptometry = new ComputerOptometryDO.ComputerOptometry()
                .setAxial(rAxial).setCyl(rCyl).setSph(rSph).setK1(rK1).setK2(rK2).setLateriality(CommonConst.RIGHT_EYE);
        ComputerOptometryDO computerOptometryDO = new ComputerOptometryDO().setRightEyeData(rightComputerOptometry).setLeftEyeData(leftComputerOptometry).setIsCooperative(getIsCooperative());
        computerOptometryDO.setDiagnosis(super.getDiagnosis());
        computerOptometryDO.setCreateUserId(getCreateUserId());
        computerOptometryDO.setUpdateTime(getUpdateTime());
        return visionScreeningResult.setComputerOptometry(computerOptometryDO);
    }

    public boolean isValid() {
        // 不配合时全部校验
        if (Objects.nonNull(getIsCooperative()) && getIsCooperative() == 1) {
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
            computerOptometryDTO.setLSe(leftEye.getCyl().multiply(BigDecimal.valueOf(0.5)).add(leftEye.getSph()).setScale(2, BigDecimal.ROUND_UP));
            computerOptometryDTO.setLK1(leftEye.getK1());
            computerOptometryDTO.setLK2(leftEye.getK2());
        }
        ComputerOptometryDO.ComputerOptometry rightEye = computerOptometryDO.getRightEyeData();
        if (Objects.nonNull(rightEye)) {
            computerOptometryDTO.setRAxial(rightEye.getAxial());
            computerOptometryDTO.setRCyl(rightEye.getCyl());
            computerOptometryDTO.setRSph(rightEye.getSph());
            computerOptometryDTO.setRSe(rightEye.getCyl().multiply(BigDecimal.valueOf(0.5)).add(rightEye.getSph()).setScale(2, BigDecimal.ROUND_UP));
            computerOptometryDTO.setRK1(rightEye.getK1());
            computerOptometryDTO.setRK2(rightEye.getK2());
        }
        computerOptometryDTO.setDiagnosis(computerOptometryDO.getDiagnosis());
        computerOptometryDTO.setIsCooperative(computerOptometryDO.getIsCooperative());
        return computerOptometryDTO;
    }

    @Override
    public String getDataType() {
        return ScreeningConstant.SCREENING_DATA_TYPE_COMPUTER_OPTOMETRY;
    }
}


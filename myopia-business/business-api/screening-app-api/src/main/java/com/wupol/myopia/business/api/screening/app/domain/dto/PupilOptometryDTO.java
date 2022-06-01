package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.dos.PupilOptometryDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 小瞳验光数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class PupilOptometryDTO extends ScreeningResultBasicData {

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
     * 矫正视力(左眼)
     */
    private BigDecimal leftCorrectedVision;
    /**
     * 矫正视力(右眼)
     */
    private BigDecimal rightCorrectedVision;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        PupilOptometryDataDO.PupilOptometryData leftPupilOptometryData = new PupilOptometryDataDO.PupilOptometryData().setAxial(lAxial).setCyl(lCyl).setSph(lSph).setCorrectedVision(leftCorrectedVision).setLateriality(CommonConst.LEFT_EYE);
        PupilOptometryDataDO.PupilOptometryData rightPupilOptometryData = new PupilOptometryDataDO.PupilOptometryData().setAxial(rAxial).setCyl(rCyl).setSph(rSph).setCorrectedVision(rightCorrectedVision).setLateriality(CommonConst.RIGHT_EYE);
        PupilOptometryDataDO pupilOptometryDataDO = new PupilOptometryDataDO().setLeftEyeData(leftPupilOptometryData).setRightEyeData(rightPupilOptometryData).setIsCooperative(getIsCooperative());
        pupilOptometryDataDO.setDiagnosis(super.getDiagnosis());
        pupilOptometryDataDO.setCreateUserId(getCreateUserId());
        return visionScreeningResult.setPupilOptometryData(pupilOptometryDataDO);
    }

    public boolean isValid() {
        return ObjectUtils.anyNotNull(rAxial, lAxial, lSph, rSph, rCyl, lCyl, leftCorrectedVision, rightCorrectedVision);
    }

    public static PupilOptometryDTO getInstance(PupilOptometryDataDO pupilOptometryDO) {
        if (Objects.isNull(pupilOptometryDO)) {
            return null;
        }
        PupilOptometryDTO pupilOptometryDTO = new PupilOptometryDTO();
        PupilOptometryDataDO.PupilOptometryData leftEye = pupilOptometryDO.getLeftEyeData();
        if (Objects.nonNull(leftEye)) {
            pupilOptometryDTO.setLAxial(leftEye.getAxial());
            pupilOptometryDTO.setLCyl(leftEye.getCyl());
            pupilOptometryDTO.setLSph(leftEye.getSph());
            pupilOptometryDTO.setLeftCorrectedVision(leftEye.getCorrectedVision());
        }
        PupilOptometryDataDO.PupilOptometryData rightEye = pupilOptometryDO.getRightEyeData();
        if (Objects.nonNull(rightEye)) {
            pupilOptometryDTO.setRAxial(rightEye.getAxial());
            pupilOptometryDTO.setRCyl(rightEye.getCyl());
            pupilOptometryDTO.setRSph(rightEye.getSph());
            pupilOptometryDTO.setRightCorrectedVision(rightEye.getCorrectedVision());
        }
        pupilOptometryDTO.setDiagnosis(pupilOptometryDO.getDiagnosis());
        pupilOptometryDTO.setIsCooperative(pupilOptometryDO.getIsCooperative());
        return pupilOptometryDTO;
    }
}


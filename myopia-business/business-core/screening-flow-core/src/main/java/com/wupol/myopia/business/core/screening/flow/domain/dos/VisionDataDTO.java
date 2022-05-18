package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @Description 视力筛查结果
 * @Date 2021/1/22 16:37
 * @Author by jacob
 */
@Data
public class VisionDataDTO extends ScreeningResultBasicData {

    /**
     * 佩戴眼镜的类型： @{link com.wupol.myopia.business.common.constant.WearingGlassesSituation}
     */
    @JsonProperty("glasses")
    @NotBlank
    private String glassesType;
    /**
     * 右眼矫正视力
     */
    @JsonProperty("r_jzsl")
    private BigDecimal rightCorrectedVision;
    /**
     * 左眼矫正视力
     */
    @JsonProperty("l_jzsl")
    private BigDecimal leftCorrectedVision;
    /**
     * 右眼裸眼视力
     */
    @JsonProperty("r_lsl")
    private BigDecimal rightNakedVision;
    /**
     * 左眼裸眼视力
     */
    @JsonProperty("l_lsl")
    private BigDecimal leftNakedVision;

    /**
     * 夜戴角膜塑形镜的度数
     */
    @JsonProperty("r_ok_degree")
    private BigDecimal rightOkDegree;
    /**
     * 夜戴角膜塑形镜的度数
     */
    @JsonProperty("l_ok_degree")
    private BigDecimal leftOkDegree;



    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        VisionDataDO.VisionData leftVisionData = new VisionDataDO.VisionData()
                .setNakedVision(leftNakedVision)
                .setCorrectedVision(leftCorrectedVision)
                .setGlassesType(WearingGlassesSituation.getKey(glassesType))
                .setLateriality(CommonConst.LEFT_EYE)
                .setOkDegree(leftOkDegree);
        VisionDataDO.VisionData rightVisionData = new VisionDataDO.VisionData()
                .setNakedVision(rightNakedVision)
                .setCorrectedVision(rightCorrectedVision)
                .setGlassesType(WearingGlassesSituation.getKey(glassesType))
                .setLateriality(CommonConst.RIGHT_EYE)
                .setOkDegree(rightOkDegree);
        VisionDataDO visionDataDO = new VisionDataDO().setRightEyeData(rightVisionData).setLeftEyeData(leftVisionData).setIsCooperative(super.getIsCooperative());
        visionDataDO.setDiagnosis(super.getDiagnosis());
        visionDataDO.setCreateUserId(getCreateUserId());
        return visionScreeningResult.setVisionData(visionDataDO);
    }

    public boolean isValid() {
        // 不配合时全部校验
        if (Objects.isNull(super.getIsCooperative()) || super.getIsCooperative() == 1) {
            return true;
        }
        // 没带眼镜
        if (glassesType.equals(WearingGlassesSituation.NOT_WEARING_GLASSES_TYPE)) {
            return Objects.nonNull(rightNakedVision) && Objects.nonNull(leftNakedVision);
        }
        if (glassesType.equals(WearingGlassesSituation.WEARING_FRAME_GLASSES_TYPE) || glassesType.equals(WearingGlassesSituation.WEARING_CONTACT_LENS_TYPE)) {
            return Objects.nonNull(rightNakedVision) && Objects.nonNull(leftNakedVision)
                    && Objects.nonNull(rightCorrectedVision) && Objects.nonNull(leftCorrectedVision);
        }
        if (glassesType.equals(WearingGlassesSituation.WEARING_OVERNIGHT_ORTHOKERATOLOGY_TYPE)) {
            return Objects.nonNull(rightCorrectedVision) && Objects.nonNull(leftCorrectedVision);
        }
        return true;
    }

    public static VisionDataDTO getInstance(VisionDataDO visionDataDO) {
        if (Objects.isNull(visionDataDO)) {
            return null;
        }
        VisionDataDTO visionDataDTO = new VisionDataDTO();
        VisionDataDO.VisionData leftEye = visionDataDO.getLeftEyeData();
        if (Objects.nonNull(leftEye)) {
            visionDataDTO.setLeftNakedVision(leftEye.getNakedVision());
            visionDataDTO.setLeftCorrectedVision(leftEye.getCorrectedVision());
            visionDataDTO.setGlassesType(WearingGlassesSituation.getType(leftEye.getGlassesType()));
            visionDataDTO.setLeftOkDegree(leftEye.getOkDegree());
        }
        VisionDataDO.VisionData rightEye = visionDataDO.getRightEyeData();
        if (Objects.nonNull(rightEye)) {
            visionDataDTO.setRightNakedVision(rightEye.getNakedVision());
            visionDataDTO.setRightCorrectedVision(rightEye.getCorrectedVision());
            visionDataDTO.setGlassesType(WearingGlassesSituation.getType(rightEye.getGlassesType()));
            visionDataDTO.setRightOkDegree(rightEye.getOkDegree());
        }
        visionDataDTO.setDiagnosis(visionDataDO.getDiagnosis());
        visionDataDTO.setIsCooperative(visionDataDO.getIsCooperative());
        return visionDataDTO;
    }

    public BigDecimal getRightCorrectedVision() {
        return BigDecimalUtil.keepDecimalPlaces(rightCorrectedVision, 1);
    }

    public BigDecimal getLeftCorrectedVision() {
        return BigDecimalUtil.keepDecimalPlaces(leftCorrectedVision, 1);
    }

    public BigDecimal getRightNakedVision() {
        return BigDecimalUtil.keepDecimalPlaces(rightNakedVision, 1);
    }

    public BigDecimal getLeftNakedVision() {
        return BigDecimalUtil.keepDecimalPlaces(leftNakedVision, 1);
    }
}


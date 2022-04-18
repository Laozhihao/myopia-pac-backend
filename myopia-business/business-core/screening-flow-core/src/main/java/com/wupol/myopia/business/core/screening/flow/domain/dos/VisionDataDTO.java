package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonProperty;
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
     * 右眼等效球镜
     */
    @JsonProperty("r_ok_degree")
    private BigDecimal rightOkDegree;
    /**
     * 左眼等效球镜
     */
    @JsonProperty("l_ok_degree")
    private BigDecimal leftOkDegree;

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
        VisionDataDO.VisionData leftVisionData = new VisionDataDO.VisionData().setNakedVision(leftNakedVision).setCorrectedVision(leftCorrectedVision).setGlassesType(WearingGlassesSituation.getKey(glassesType)).setLateriality(CommonConst.LEFT_EYE);
        VisionDataDO.VisionData rightVisionData = new VisionDataDO.VisionData().setNakedVision(rightNakedVision).setCorrectedVision(rightCorrectedVision).setGlassesType(WearingGlassesSituation.getKey(glassesType)).setLateriality(CommonConst.RIGHT_EYE);
        VisionDataDO visionDataDO = new VisionDataDO().setRightEyeData(rightVisionData).setLeftEyeData(leftVisionData).setIsCooperative(isCooperative);
        visionDataDO.setDiagnosis(diagnosis);
        visionDataDO.setCreateUserId(getCreateUserId());
        return visionScreeningResult.setVisionData(visionDataDO);
    }

    public boolean isValid() {
        return ObjectUtils.anyNotNull(rightNakedVision, leftNakedVision, rightCorrectedVision, leftCorrectedVision);
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
}


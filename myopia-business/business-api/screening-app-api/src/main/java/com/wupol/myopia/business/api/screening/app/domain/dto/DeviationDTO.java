package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dos.DeviationDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * @Description 误差说明
 * @Date 2021/04/07 1:08
 * @Author by xz
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DeviationDTO extends ScreeningResultBasicData {
    /**
     * 视力或屈光检查误差
     */
    private Integer visionOrOptometryDeviationType;

    private String visionOrOptometryDeviationRemark;

    /**
     * 身高体重误差
     */
    private Integer heightWeightDeviationType;

    private String heightWeightDeviationRemark;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        DeviationDO deviationDO = new DeviationDO();
        DeviationDO.HeightWeightDeviation heightWeightDeviation = null;
        DeviationDO.VisionOrOptometryDeviation visionOrOptometryDeviation = null;

        if (Objects.nonNull(visionOrOptometryDeviationType)) {
            visionOrOptometryDeviation = new DeviationDO.VisionOrOptometryDeviation();
            visionOrOptometryDeviation.setType(DeviationDO.VisionOrOptometryDeviationEnum.getByCode(visionOrOptometryDeviationType));
            visionOrOptometryDeviation.setRemark(visionOrOptometryDeviationRemark);
        }
        if (Objects.nonNull(heightWeightDeviationType)) {
            heightWeightDeviation = new DeviationDO.HeightWeightDeviation();
            heightWeightDeviation.setType(DeviationDO.HeightWeightDeviationEnum.getByCode(heightWeightDeviationType));
            heightWeightDeviation.setRemark(heightWeightDeviationRemark);
        }
        deviationDO.setHeightWeightDeviation(heightWeightDeviation);
        deviationDO.setVisionOrOptometryDeviation(visionOrOptometryDeviation);
        deviationDO.setDiagnosis(super.getDiagnosis());
        deviationDO.setCreateUserId(getCreateUserId());
        deviationDO.setUpdateTime(getUpdateTime());
        return visionScreeningResult.setDeviationData(deviationDO);
    }

    public boolean isValid() {
        // 暂时不需要验证，如果为空就是正常的
        return !Objects.isNull(DeviationDO.VisionOrOptometryDeviationEnum.getByCode(visionOrOptometryDeviationType))
                && !Objects.isNull(DeviationDO.HeightWeightDeviationEnum.getByCode(visionOrOptometryDeviationType));
    }

    public static DeviationDTO getInstance(DeviationDO deviationDO) {
        if (Objects.isNull(deviationDO)) {
            return null;
        }
        DeviationDTO deviationDTO = new DeviationDTO();
        deviationDTO.setHeightWeightDeviationType(Objects.nonNull(deviationDO.getHeightWeightDeviation()) ? deviationDO.getHeightWeightDeviation().getType().getCode() : null);
        deviationDTO.setHeightWeightDeviationRemark(Objects.nonNull(deviationDO.getHeightWeightDeviation()) ? deviationDO.getHeightWeightDeviation().getRemark() : null);
        deviationDTO.setVisionOrOptometryDeviationType(Objects.nonNull(deviationDO.getVisionOrOptometryDeviation()) ? deviationDO.getVisionOrOptometryDeviation().getType().getCode() : null);
        deviationDTO.setVisionOrOptometryDeviationRemark(Objects.nonNull(deviationDO.getVisionOrOptometryDeviation()) ? deviationDO.getVisionOrOptometryDeviation().getRemark() : null);
        return deviationDTO;
    }

    @Override
    public String getDataType() {
        return ScreeningConstant.SCREENING_DATA_TYPE_DEVIATION;
    }
}

package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.EyePressureDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;

/**
 * 眼压数据
 *
 * @Author tastyb
 * @Date 2022/2/16
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class HeightAndWeightDataDTO extends ScreeningResultBasicData {
    /**
     * 身高
     */
    private String height;
    /**
     * 体重
     */
    private String weight;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        HeightAndWeightDataDO heightAndWeightDataDO = new HeightAndWeightDataDO();
        heightAndWeightDataDO.setHeight(height);
        heightAndWeightDataDO.setWeight(weight);
        heightAndWeightDataDO.setCreateUserId(getCreateUserId());
        return visionScreeningResult.setHeightAndWeightData(heightAndWeightDataDO);
    }

    public boolean isValid() {
        return ObjectUtils.anyNotNull(height, weight);
    }

    public static HeightAndWeightDataDTO getInstance(HeightAndWeightDataDO heightAndWeightDataDO) {
        if (Objects.isNull(heightAndWeightDataDO)) {
            return null;
        }
        HeightAndWeightDataDTO heightAndWeightDataDTO = new HeightAndWeightDataDTO();
        if (Objects.nonNull(heightAndWeightDataDO.getHeight())) {
            heightAndWeightDataDTO.setHeight(heightAndWeightDataDO.getHeight());
        }
        if (Objects.nonNull(heightAndWeightDataDO.getWeight())) {
            heightAndWeightDataDTO.setWeight(heightAndWeightDataDO.getWeight());
        }
        return heightAndWeightDataDTO;
    }

}

package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.EyePressureDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.HeightAndWeightDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
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
    private BigDecimal height;
    /**
     * 体重
     */
    private BigDecimal weight;

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
        heightAndWeightDataDTO.setHeight(heightAndWeightDataDO.getHeight());
        heightAndWeightDataDTO.setWeight(heightAndWeightDataDO.getWeight());
        return heightAndWeightDataDTO;
    }

}

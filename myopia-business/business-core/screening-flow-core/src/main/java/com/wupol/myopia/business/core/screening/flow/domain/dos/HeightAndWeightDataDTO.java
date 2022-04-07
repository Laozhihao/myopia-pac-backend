package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 眼压数据
 *
 * @Author tastyb
 * @Date 2022/2/16
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HeightAndWeightDataDTO extends ScreeningResultBasicData {
    /**
     * 身高
     */
    private BigDecimal height;
    /**
     * 体重
     */
    private BigDecimal weight;

    /**
     * 身体质量指数值
     */
    private BigDecimal bmi;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        HeightAndWeightDataDO heightAndWeightDataDO = new HeightAndWeightDataDO();
        heightAndWeightDataDO.setHeight(height);
        heightAndWeightDataDO.setWeight(weight);
        heightAndWeightDataDO.setBmi(bmi);
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
        heightAndWeightDataDTO.setBmi(heightAndWeightDataDO.getBmi());
        return heightAndWeightDataDTO;
    }

}

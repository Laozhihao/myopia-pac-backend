package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 身高体重数据
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
     * 身高范围
     */
    private static final List<BigDecimal> HEIGHT_RANGE = Lists.newArrayList(BigDecimal.ZERO, new BigDecimal(270));

    /**
     * 体重范围
     */
    private static final List<BigDecimal> WEIGHT_RANGE = Lists.newArrayList(BigDecimal.ZERO, new BigDecimal(200));
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
        heightAndWeightDataDO.setDiagnosis(super.getDiagnosis());
        heightAndWeightDataDO.setUpdateTime(getUpdateTime());
        return visionScreeningResult.setHeightAndWeightData(heightAndWeightDataDO);
    }

    public boolean isValid() {
        // 都为空返回false。身高为不为空不在范围直接返回空，符合再判断身高
        boolean status = false;
        if (ObjectUtils.anyNotNull(height, weight)) {
            // 身高不为空且在范围内
            if (Objects.nonNull(height)) {
                if (height.compareTo(HEIGHT_RANGE.get(0)) > -1
                        && height.compareTo(HEIGHT_RANGE.get(1)) < 1) {
                    status = true;
                } else {
                    return false;
                }
            }
            // 体重不为空在范围内
            if (Objects.nonNull(weight)) {
                if(weight.compareTo(WEIGHT_RANGE.get(0)) > -1
                        && weight.compareTo(WEIGHT_RANGE.get(1)) < 1){
                    status = true;
                }else{
                    return false;
                }
            }
        }
        return status;
    }

    public static HeightAndWeightDataDTO getInstance(HeightAndWeightDataDO heightAndWeightDataDO) {
        if (Objects.isNull(heightAndWeightDataDO)) {
            return null;
        }
        HeightAndWeightDataDTO heightAndWeightDataDTO = new HeightAndWeightDataDTO();
        heightAndWeightDataDTO.setHeight(BigDecimalUtil.getBigDecimalByFormat(heightAndWeightDataDO.getHeight(),1));
        heightAndWeightDataDTO.setWeight(BigDecimalUtil.getBigDecimalByFormat(heightAndWeightDataDO.getWeight(),1));
        heightAndWeightDataDTO.setBmi(heightAndWeightDataDO.getBmi());
        return heightAndWeightDataDTO;
    }

    @Override
    public String getDataType() {
        return ScreeningConstant.SCREENING_DATA_TYPE_HEIGHT_WEIGHT;
    }

}

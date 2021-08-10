package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.EyePressureDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 眼压数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class EyePressureDataDTO extends ScreeningResultBasicData {
    /**
     * 左眼压
     */
    private BigDecimal leftPressure;
    /**
     * 右眼压
     */
    private BigDecimal rightPressure;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        EyePressureDataDO.EyePressureData leftEyePressureData = new EyePressureDataDO.EyePressureData().setLateriality(0).setPressure(leftPressure);
        EyePressureDataDO.EyePressureData rightEyePressureData = new EyePressureDataDO.EyePressureData().setLateriality(1).setPressure(rightPressure);
        EyePressureDataDO eyePressureDataDO = new EyePressureDataDO().setLeftEyeData(leftEyePressureData).setRightEyeData(rightEyePressureData).setIsCooperative(isCooperative);
        return visionScreeningResult.setEyePressureData(eyePressureDataDO);
    }

    public boolean isValid() {
        return Objects.nonNull(leftPressure) || Objects.nonNull(rightPressure);
    }
}


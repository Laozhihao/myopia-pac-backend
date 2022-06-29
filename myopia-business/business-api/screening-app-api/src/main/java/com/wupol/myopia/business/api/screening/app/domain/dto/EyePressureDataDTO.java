package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dos.EyePressureDataDO;
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


    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        EyePressureDataDO.EyePressureData leftEyePressureData = new EyePressureDataDO.EyePressureData().setLateriality(CommonConst.LEFT_EYE).setPressure(leftPressure);
        EyePressureDataDO.EyePressureData rightEyePressureData = new EyePressureDataDO.EyePressureData().setLateriality(CommonConst.RIGHT_EYE).setPressure(rightPressure);
        EyePressureDataDO eyePressureDataDO = new EyePressureDataDO().setLeftEyeData(leftEyePressureData).setRightEyeData(rightEyePressureData).setIsCooperative(getIsCooperative());
        eyePressureDataDO.setCreateUserId(getCreateUserId());
        eyePressureDataDO.setDiagnosis(super.getDiagnosis());
        eyePressureDataDO.setUpdateTime(getUpdateTime());
        return visionScreeningResult.setEyePressureData(eyePressureDataDO);
    }

    public boolean isValid() {
        return ObjectUtils.anyNotNull(leftPressure, rightPressure);
    }

    public static EyePressureDataDTO getInstance(EyePressureDataDO eyePressureDataDO) {
        if (Objects.isNull(eyePressureDataDO)) {
            return null;
        }
        EyePressureDataDTO eyePressureDataDTO = new EyePressureDataDTO();
        EyePressureDataDO.EyePressureData leftEye = eyePressureDataDO.getLeftEyeData();
        if (Objects.nonNull(leftEye)) {
            eyePressureDataDTO.setLeftPressure(leftEye.getPressure());
        }
        EyePressureDataDO.EyePressureData rightEye = eyePressureDataDO.getRightEyeData();
        if (Objects.nonNull(rightEye)) {
            eyePressureDataDTO.setRightPressure(rightEye.getPressure());
        }
        eyePressureDataDTO.setIsCooperative(eyePressureDataDO.getIsCooperative());
        return eyePressureDataDTO;
    }

    @Override
    public String getDataType() {
        return ScreeningConstant.SCREENING_DATA_TYPE_EYE_PRESSURE;
    }
}


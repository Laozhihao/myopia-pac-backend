package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.IntraocularPressureDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 眼底
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class IntraocularPressureDataDTO extends ScreeningResultBasicData {
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
        IntraocularPressureDataDO.IntraocularPressureData leftIntraocularPressureData = new IntraocularPressureDataDO.IntraocularPressureData().setLateriality(0).setPressure(leftPressure);
        IntraocularPressureDataDO.IntraocularPressureData rightIntraocularPressureData = new IntraocularPressureDataDO.IntraocularPressureData().setLateriality(1).setPressure(rightPressure);
        IntraocularPressureDataDO intraocularPressureDataDO = new IntraocularPressureDataDO().setLeftEyeData(leftIntraocularPressureData).setRightEyeData(rightIntraocularPressureData).setIsCooperative(isCooperative);
        return visionScreeningResult.setIntraocularPressureData(intraocularPressureDataDO);
    }
}


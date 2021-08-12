package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.dos.FundusDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.OcularInspectionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SlitLampDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SlitLampDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 复合检查数据（眼位、裂隙灯、眼底）
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class MultiCheckDataDTO extends ScreeningResultBasicData {
    /**
     * 眼位
     **/
    private OcularInspectionDataDTO ocularInspectionData;
    /**
     * 裂隙灯
     **/
    private SlitLampDataDTO slitLampData;
    /**
     * 眼底
     **/
    private FundusDataDTO fundusData;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        // 33cm眼位
        OcularInspectionDataDO ocularInspectionDataDO = new OcularInspectionDataDO()
                .setEsotropia(ocularInspectionData.getEsotropia())
                .setExotropia(ocularInspectionData.getExotropia())
                .setVerticalStrabismus(ocularInspectionData.getVerticalStrabismus())
                .setDiagnosis(ocularInspectionData.getDiagnosis())
                .setIsCooperative(isCooperative);
        // 裂隙灯
        SlitLampDataDO.SlitLampData leftSlitLampData = new SlitLampDataDO.SlitLampData().setLateriality(CommonConst.LEFT_EYE).setDiagnosis(slitLampData.getLeftDiagnosis()).setPathologicalTissues(slitLampData.getLeftPathologicalTissueList());
        SlitLampDataDO.SlitLampData rightSlitLampData = new SlitLampDataDO.SlitLampData().setLateriality(CommonConst.RIGHT_EYE).setDiagnosis(slitLampData.getRightDiagnosis()).setPathologicalTissues(slitLampData.getRightPathologicalTissueList());
        SlitLampDataDO slitLampDataDO = new SlitLampDataDO().setRightEyeData(rightSlitLampData).setLeftEyeData(leftSlitLampData).setIsCooperative(isCooperative);
        // 眼底
        FundusDataDO.FundusData leftFundusData = new FundusDataDO.FundusData().setLateriality(CommonConst.LEFT_EYE).setHasAbnormal(fundusData.getLeftHasAbnormal());
        FundusDataDO.FundusData rightFundusData = new FundusDataDO.FundusData().setLateriality(CommonConst.RIGHT_EYE).setHasAbnormal(fundusData.getRightHasAbnormal());
        FundusDataDO fundusDataDO = new FundusDataDO().setLeftEyeData(leftFundusData).setRightEyeData(rightFundusData).setIsCooperative(isCooperative).setRemark(fundusData.getRemark());
        return visionScreeningResult.setOcularInspectionData(ocularInspectionDataDO).setSlitLampData(slitLampDataDO).setFundusData(fundusDataDO);
    }
}

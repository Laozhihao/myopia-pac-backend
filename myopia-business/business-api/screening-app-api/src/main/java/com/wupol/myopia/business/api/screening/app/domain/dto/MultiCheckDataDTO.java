package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.dos.FundusDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.OcularInspectionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SlitLampDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisualLossLevelDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SlitLampDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;

/**
 * 复合检查数据（眼位、裂隙灯、眼底、盲及视力损害等级）
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
     * 盲及视力损害分类（等级）
     **/
    private VisualLossLevelDataDTO visualLossLevelData;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        // 33cm眼位
        if (Objects.nonNull(ocularInspectionData)) {
            OcularInspectionDataDO ocularInspectionDataDO = new OcularInspectionDataDO()
                    .setEsotropia(ocularInspectionData.getEsotropia())
                    .setExotropia(ocularInspectionData.getExotropia())
                    .setVerticalStrabismus(ocularInspectionData.getVerticalStrabismus())
                    .setIsCooperative(isCooperative);
            ocularInspectionDataDO.setDiagnosis(ocularInspectionData.getDiagnosis());
            ocularInspectionDataDO.setCreateUserId(getCreateUserId());
            visionScreeningResult.setOcularInspectionData(ocularInspectionDataDO);
        }
        // 裂隙灯
        if (Objects.nonNull(slitLampData)) {
            SlitLampDataDO.SlitLampData leftSlitLampData = new SlitLampDataDO.SlitLampData().setLateriality(CommonConst.LEFT_EYE).setPathologicalTissues(slitLampData.getLeftPathologicalTissueList());
            leftSlitLampData.setDiagnosis(slitLampData.getLeftDiagnosis());
            SlitLampDataDO.SlitLampData rightSlitLampData = new SlitLampDataDO.SlitLampData().setLateriality(CommonConst.RIGHT_EYE).setPathologicalTissues(slitLampData.getRightPathologicalTissueList());
            rightSlitLampData.setDiagnosis(slitLampData.getRightDiagnosis());
            SlitLampDataDO slitLampDataDO = new SlitLampDataDO().setRightEyeData(rightSlitLampData).setLeftEyeData(leftSlitLampData).setIsCooperative(isCooperative);
            slitLampDataDO.setCreateUserId(getCreateUserId());
            visionScreeningResult.setSlitLampData(slitLampDataDO);
        }
        // 眼底
        if (Objects.nonNull(fundusData)) {
            FundusDataDO.FundusData leftFundusData = new FundusDataDO.FundusData().setLateriality(CommonConst.LEFT_EYE).setHasAbnormal(fundusData.getLeftHasAbnormal());
            FundusDataDO.FundusData rightFundusData = new FundusDataDO.FundusData().setLateriality(CommonConst.RIGHT_EYE).setHasAbnormal(fundusData.getRightHasAbnormal());
            FundusDataDO fundusDataDO = new FundusDataDO().setLeftEyeData(leftFundusData).setRightEyeData(rightFundusData).setIsCooperative(isCooperative).setRemark(fundusData.getRemark());
            fundusDataDO.setCreateUserId(getCreateUserId());
            visionScreeningResult.setFundusData(fundusDataDO);
        }
        // 盲及视力损害分类
        if (Objects.nonNull(visualLossLevelData)) {
            VisualLossLevelDataDO.VisualLossLevelData leftVisualLossLevelData = new VisualLossLevelDataDO.VisualLossLevelData().setLateriality(CommonConst.LEFT_EYE).setLevel(visualLossLevelData.getLeftVisualLossLevel());
            VisualLossLevelDataDO.VisualLossLevelData rightVisualLossLevelData = new VisualLossLevelDataDO.VisualLossLevelData().setLateriality(CommonConst.RIGHT_EYE).setLevel(visualLossLevelData.getRightVisualLossLevel());
            VisualLossLevelDataDO visualLossLevelDataDO = new VisualLossLevelDataDO().setLeftEyeData(leftVisualLossLevelData).setRightEyeData(rightVisualLossLevelData).setIsCooperative(isCooperative);
            visualLossLevelDataDO.setCreateUserId(getCreateUserId());
            visionScreeningResult.setVisualLossLevelData(visualLossLevelDataDO);
        }
        return visionScreeningResult;
    }

    public boolean isValid() {
        return ObjectUtils.anyNotNull(ocularInspectionData, slitLampData, fundusData, visualLossLevelData);
    }

    public static MultiCheckDataDTO getInstance(OcularInspectionDataDO ocularInspectionDataDO, FundusDataDO fundusDataDO, SlitLampDataDO slitLampDataDO, VisualLossLevelDataDO visualLossLevelDataDO) {
        MultiCheckDataDTO multiCheckDataDTO = new MultiCheckDataDTO();
        // 眼位
        multiCheckDataDTO.setOcularInspectionData(OcularInspectionDataDTO.getInstance(ocularInspectionDataDO));
        // 眼底
        multiCheckDataDTO.setFundusData(FundusDataDTO.getInstance(fundusDataDO));
        // 裂隙灯
        multiCheckDataDTO.setSlitLampData(SlitLampDataDTO.getInstance(slitLampDataDO));
        // 盲及视力损害分类（等级）
        multiCheckDataDTO.setVisualLossLevelData(VisualLossLevelDataDTO.getInstance(visualLossLevelDataDO));
        multiCheckDataDTO.setIsCooperative(getCooperative(ocularInspectionDataDO, fundusDataDO, slitLampDataDO, visualLossLevelDataDO));
        return multiCheckDataDTO;
    }

    private static Integer getCooperative(OcularInspectionDataDO ocularInspectionDataDO, FundusDataDO fundusDataDO, SlitLampDataDO slitLampDataDO, VisualLossLevelDataDO visualLossLevelDataDO) {
        if (Objects.nonNull(ocularInspectionDataDO) && Objects.nonNull(ocularInspectionDataDO.getIsCooperative())) {
            return ocularInspectionDataDO.getIsCooperative();
        }
        if (Objects.nonNull(fundusDataDO) && Objects.nonNull(fundusDataDO.getIsCooperative())) {
            return fundusDataDO.getIsCooperative();
        }
        if (Objects.nonNull(slitLampDataDO) && Objects.nonNull(slitLampDataDO.getIsCooperative())) {
            return slitLampDataDO.getIsCooperative();
        }
        if (Objects.nonNull(visualLossLevelDataDO) && Objects.nonNull(visualLossLevelDataDO.getIsCooperative())) {
            return visualLossLevelDataDO.getIsCooperative();
        }
        return null;
    }
}

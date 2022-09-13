package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.dos.FundusDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.OcularInspectionDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SlitLampDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 裂隙灯检查数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class SlitLampDataDTO extends ScreeningResultBasicData implements Serializable {

    /**
     * 病变异常组织(左眼)
     */
    private String leftPathologicalTissues;

    /**
     * 病变异常组织(右眼)
     */
    private String rightPathologicalTissues;

    /**
     * 初步诊断结果(左眼)：0-正常、1-（疑似）异常
     */
    private Integer leftDiagnosis;

    /**
     * 初步诊断结果（右眼）：0-正常、1-（疑似）异常
     */
    private Integer rightDiagnosis;

    /**
     * 获取右眼病变组织list
     * @return java.util.List<java.lang.String>
     */
    public List<String> getRightPathologicalTissueList() {
        return this.getPathologicalTissueList(rightPathologicalTissues);
    }

    /**
     * 获取左眼病变组织list
     * @return java.util.List<java.lang.String>
     */
    public List<String> getLeftPathologicalTissueList() {
        return this.getPathologicalTissueList(leftPathologicalTissues);
    }

    /**
     * 获取list
     * @param pathologicalTissueStr 逗号分隔的病变组织字符串
     * @return java.util.List<java.lang.String>
     */
    private List<String> getPathologicalTissueList(String pathologicalTissueStr) {
        if (StringUtils.isBlank(pathologicalTissueStr)) {
            return new ArrayList<>();
        }
        String[] diseaseStringArray = pathologicalTissueStr.split(",");
        return Arrays.asList(diseaseStringArray);
    }

    public static SlitLampDataDTO getInstance(SlitLampDataDO slitLampDataDO) {
        if (Objects.isNull(slitLampDataDO)) {
            return null;
        }
        SlitLampDataDTO slitLampDataDTO = new SlitLampDataDTO();
        SlitLampDataDO.SlitLampData leftEye = slitLampDataDO.getLeftEyeData();
        if (Objects.nonNull(leftEye)) {
            slitLampDataDTO.setLeftDiagnosis(leftEye.getDiagnosis());
            slitLampDataDTO.setLeftPathologicalTissues(StringUtils.join(leftEye.getPathologicalTissues(), ","));
        }
        SlitLampDataDO.SlitLampData rightEye = slitLampDataDO.getRightEyeData();
        if (Objects.nonNull(rightEye)) {
            slitLampDataDTO.setRightDiagnosis(rightEye.getDiagnosis());
            slitLampDataDTO.setRightPathologicalTissues(StringUtils.join(rightEye.getPathologicalTissues(), ","));
        }
        slitLampDataDTO.setIsCooperative(slitLampDataDO.getIsCooperative());
        return slitLampDataDTO;
    }

    public boolean isValid() {
        return ObjectUtils.anyNotNull(leftPathologicalTissues, rightPathologicalTissues);
    }

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        SlitLampDataDO slitLampDataDO = new SlitLampDataDO();
        SlitLampDataDO.SlitLampData leftData = new SlitLampDataDO.SlitLampData();
        leftData.setLateriality(CommonConst.LEFT_EYE).setPathologicalTissues(getLeftPathologicalTissueList()).setDiagnosis(leftDiagnosis);
        SlitLampDataDO.SlitLampData rightData = new SlitLampDataDO.SlitLampData();
        rightData.setLateriality(CommonConst.RIGHT_EYE).setPathologicalTissues(getRightPathologicalTissueList());
        slitLampDataDO.setLeftEyeData(leftData).setRightEyeData(rightData).setIsCooperative(getIsCooperative()).setDiagnosis(rightDiagnosis);
        slitLampDataDO.setCreateUserId(getCreateUserId());
        slitLampDataDO.setUpdateTime(getUpdateTime());
        visionScreeningResult.setSlitLampData(slitLampDataDO);
        return visionScreeningResult;
    }

    @Override
    public String getDataType() {
        return ScreeningConstant.SCREENING_DATA_TYPE_SLIT_LAMP;
    }
}

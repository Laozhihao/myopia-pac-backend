package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.SlitLampDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 裂隙灯检查数据
 *
 * @Author HaoHao
 * @Date 2021/7/27
 **/
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class SlitLampDataDTO extends ScreeningResultBasicData {
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
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

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

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        SlitLampDataDO.SlitLampData rightSlitLampData = new SlitLampDataDO.SlitLampData().setLateriality(1).setDiagnosis(rightDiagnosis).setPathologicalTissues(getRightPathologicalTissueList());
        SlitLampDataDO.SlitLampData leftSlitLampData = new SlitLampDataDO.SlitLampData().setLateriality(0).setDiagnosis(leftDiagnosis).setPathologicalTissues(getLeftPathologicalTissueList());
        SlitLampDataDO slitLampDataDO = new SlitLampDataDO().setRightEyeData(rightSlitLampData).setLeftEyeData(leftSlitLampData).setIsCooperative(isCooperative);
        return visionScreeningResult.setSlitLampData(slitLampDataDO);
    }

}

package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.dos.SlitLampDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
        SlitLampDataDO.SlitLampData rightSlitLampData = new SlitLampDataDO.SlitLampData().setPathologicalTissues(getRightPathologicalTissueList()).setLateriality(1);
        SlitLampDataDO.SlitLampData leftSlitLampData = new SlitLampDataDO.SlitLampData().setPathologicalTissues(getLeftPathologicalTissueList()).setLateriality(0);
        SlitLampDataDO slitLampDataDO = new SlitLampDataDO().setRightEyeData(rightSlitLampData).setLeftEyeData(leftSlitLampData);
        return visionScreeningResult.setSlitLampData(slitLampDataDO);
    }

}

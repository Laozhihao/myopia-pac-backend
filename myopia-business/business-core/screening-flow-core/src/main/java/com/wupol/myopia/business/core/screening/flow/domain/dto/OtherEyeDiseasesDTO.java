package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.business.core.screening.flow.domain.dos.OtherEyeDiseasesDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.VisualLossLevelDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description 其他眼病  左右眼起码要有一只眼有疾病
 * @Date 2021/1/26 1:08
 * @Author by Jacob
 */

@Setter
public class OtherEyeDiseasesDTO extends ScreeningResultBasicData {

    /**
     * 眼病(左眼)
     */
    @JsonProperty("l_disease")
    private String lDiseaseStr;

    /**
     * 眼病(右眼)
     */
    @JsonProperty("r_disease")
    private String rDiseaseStr;

    /**
     * 筛查结果--全身疾病在眼部的表现
     */
    private String systemicDiseaseSymptom;

    /**
     * 盲及视力损害分类（等级）：0~9 级
     */
    private Integer leftVisualLossLevel;

    /**
     * 盲及视力损害分类（等级）：0~9 级
     */
    private Integer rightVisualLossLevel;

    /**
     * 获取右边疾病list
     * @return
     */
    public List<String> getRightDiseaseStrList() {
        return this.getDiseaseStrList(rDiseaseStr);
    }

    /**
     * 获取右边疾病list
     * @return
     */
    public List<String> getLeftDiseaseStrList() {
        return this.getDiseaseStrList(lDiseaseStr);
    }

    /**
     * 获取list
     * @param diseaseStr
     * @return
     */
    private List<String> getDiseaseStrList(String diseaseStr) {
        if (StringUtils.isBlank(diseaseStr)) {
            return new ArrayList<>();
        }
        String[] diseaseStringArray = diseaseStr.split(",");
        return Arrays.asList(diseaseStringArray);
    }

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        // 眼部疾病
        OtherEyeDiseasesDO.OtherEyeDiseases rightOtherEyeDiseases = new OtherEyeDiseasesDO.OtherEyeDiseases().setEyeDiseases(getRightDiseaseStrList()).setLateriality(1);
        OtherEyeDiseasesDO.OtherEyeDiseases leftOtherEyeDiseases = new OtherEyeDiseasesDO.OtherEyeDiseases().setEyeDiseases(getLeftDiseaseStrList()).setLateriality(0);
        OtherEyeDiseasesDO otherEyeDiseasesDO = new OtherEyeDiseasesDO().setRightEyeData(rightOtherEyeDiseases).setLeftEyeData(leftOtherEyeDiseases);
        // 损失等级
        VisualLossLevelDataDO.VisualLossLevelData leftVisualLossLevelData = new VisualLossLevelDataDO.VisualLossLevelData().setLateriality(0).setLevel(leftVisualLossLevel);
        VisualLossLevelDataDO.VisualLossLevelData rightVisualLossLevelData = new VisualLossLevelDataDO.VisualLossLevelData().setLateriality(1).setLevel(rightVisualLossLevel);
        VisualLossLevelDataDO visualLossLevelDataDO = new VisualLossLevelDataDO().setLeftEyeData(leftVisualLossLevelData).setRightEyeData(rightVisualLossLevelData);
        return visionScreeningResult.setOtherEyeDiseases(otherEyeDiseasesDO).setSystemicDiseaseSymptom(systemicDiseaseSymptom).setVisualLossLevelData(visualLossLevelDataDO);
    }

}

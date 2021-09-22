package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.screening.flow.domain.dos.OtherEyeDiseasesDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Description 其他眼病  左右眼起码要有一只眼有疾病
 * @Date 2021/1/26 1:08
 * @Author by Jacob
 */
@EqualsAndHashCode(callSuper = true)
@Data
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
        OtherEyeDiseasesDO.OtherEyeDiseases rightOtherEyeDiseases = new OtherEyeDiseasesDO.OtherEyeDiseases().setEyeDiseases(getRightDiseaseStrList()).setLateriality(CommonConst.RIGHT_EYE);
        OtherEyeDiseasesDO.OtherEyeDiseases leftOtherEyeDiseases = new OtherEyeDiseasesDO.OtherEyeDiseases().setEyeDiseases(getLeftDiseaseStrList()).setLateriality(CommonConst.LEFT_EYE);
        OtherEyeDiseasesDO otherEyeDiseasesDO = new OtherEyeDiseasesDO().setRightEyeData(rightOtherEyeDiseases).setLeftEyeData(leftOtherEyeDiseases);
        otherEyeDiseasesDO.setCreateUserId(getCreateUserId());
        // 全身疾病在眼部的表现
        return visionScreeningResult.setOtherEyeDiseases(otherEyeDiseasesDO).setSystemicDiseaseSymptom(systemicDiseaseSymptom);
    }

    public static OtherEyeDiseasesDTO getInstance(OtherEyeDiseasesDO otherEyeDiseasesDO, String systemicDiseaseSymptom) {
        if (Objects.isNull(otherEyeDiseasesDO)) {
            return null;
        }
        OtherEyeDiseasesDTO otherEyeDiseasesDTO = new OtherEyeDiseasesDTO();
        OtherEyeDiseasesDO.OtherEyeDiseases leftEye = otherEyeDiseasesDO.getLeftEyeData();
        if (Objects.nonNull(leftEye)) {
            otherEyeDiseasesDTO.setLDiseaseStr(StringUtils.join(leftEye.getEyeDiseases(), ","));
        }
        OtherEyeDiseasesDO.OtherEyeDiseases rightEye = otherEyeDiseasesDO.getRightEyeData();
        if (Objects.nonNull(rightEye)) {
            otherEyeDiseasesDTO.setRDiseaseStr(StringUtils.join(rightEye.getEyeDiseases(), ","));
        }
        otherEyeDiseasesDTO.setSystemicDiseaseSymptom(systemicDiseaseSymptom);
        return otherEyeDiseasesDTO;
    }

}

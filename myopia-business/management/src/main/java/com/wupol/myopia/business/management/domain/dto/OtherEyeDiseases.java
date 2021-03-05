package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.dos.OtherEyeDiseasesDO;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @Description 其他眼病  左右眼起码要有一只眼有疾病
 * @Date 2021/1/26 1:08
 * @Author by Jacob
 */
@Data
public class OtherEyeDiseases extends ScreeningResultBasicData {
    /**
     * 右眼疾病
     */
    private List<String> rDisease;
    /**
     * 左眼疾病
     */
    private List<String> lDisease;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        OtherEyeDiseasesDO.OtherEyeDiseases rightOtherEyeDiseases = new OtherEyeDiseasesDO.OtherEyeDiseases().setEyeDiseases(rDisease).setLateriality(1);
        OtherEyeDiseasesDO.OtherEyeDiseases leftOtherEyeDiseases = new OtherEyeDiseasesDO.OtherEyeDiseases().setEyeDiseases(lDisease).setLateriality(0);
        OtherEyeDiseasesDO otherEyeDiseasesDO = new OtherEyeDiseasesDO().setRightEyeData(rightOtherEyeDiseases).setLeftEyeData(leftOtherEyeDiseases);
        return visionScreeningResult.setOtherEyeDiseases(otherEyeDiseasesDO);
    }

}

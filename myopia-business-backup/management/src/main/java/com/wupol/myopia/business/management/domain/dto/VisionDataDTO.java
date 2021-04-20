package com.wupol.myopia.business.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wupol.myopia.business.common.constant.WearingGlassesSituation;
import com.wupol.myopia.business.management.domain.dos.VisionDataDO;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description 视力筛查结果
 * @Date 2021/1/22 16:37
 * @Author by jacob
 */
@Data
public class VisionDataDTO extends ScreeningResultBasicData {

    /**
     * 佩戴眼镜的类型： @{link com.wupol.myopia.business.common.constant.WearingGlassesSituation}
     */
    @JsonProperty("glasses")
    private String glassesType;
    /**
     * 右眼矫正视力
     */
    @JsonProperty("r_jzsl")
    private BigDecimal rightCorrectedVision;
    /**
     * 左眼矫正视力
     */
    @JsonProperty("l_jzsl")
    private BigDecimal leftCorrectedVision;
    /**
     * 右眼裸眼视力
     */
    @JsonProperty("r_lsl")
    private BigDecimal rightNakedVision;
    /**
     * 左眼裸眼视力
     */
    @JsonProperty("l_lsl")
    private BigDecimal leftNakedVision;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        VisionDataDO.VisionData leftVisionData = new VisionDataDO.VisionData().setNakedVision(leftNakedVision).setCorrectedVision(leftCorrectedVision).setGlassesType(WearingGlassesSituation.getKey(glassesType)).setLateriality(0);
        VisionDataDO.VisionData rightVisionData = new VisionDataDO.VisionData().setNakedVision(rightNakedVision).setCorrectedVision(rightCorrectedVision).setGlassesType(WearingGlassesSituation.getKey(glassesType)).setLateriality(1);
        VisionDataDO visionDataDO = new VisionDataDO().setRightEyeData(rightVisionData).setLeftEyeData(leftVisionData);
        return visionScreeningResult.setVisionData(visionDataDO);
    }
}


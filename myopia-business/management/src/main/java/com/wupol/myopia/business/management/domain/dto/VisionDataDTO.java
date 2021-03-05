package com.wupol.myopia.business.management.domain.dto;

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
     * 佩戴眼镜的类型： @{link com.myopia.common.constant.WearingGlassesSituation}
     */
    private String glassesType;
    /**
     * 右眼矫正视力
     */
    private BigDecimal rightCorrectedVision;
    /**
     * 左眼矫正视力
     */
    private BigDecimal leftCorrectedVision;
    /**
     * 右眼裸眼视力
     */
    private BigDecimal rightNakedVision;
    /**
     * 左眼裸眼视力
     */
    private BigDecimal leftNakedVision;
    /**
     * keySchoolId 未知
     */
    private Integer keySchoolId;
    /**
     * keyGrade 未知
     */
    private String keyGrade;
    /**
     * keyClazz 未知
     */
    private String keyClazz;

    @Override
    public VisionScreeningResult buildScreeningResultData(VisionScreeningResult visionScreeningResult) {
        VisionDataDO.VisionData leftVisionData = new VisionDataDO.VisionData().setNakedVision(leftNakedVision).setCorrectedVision(leftCorrectedVision).setGlassesType(glassesType).setLateriality(0);
        VisionDataDO.VisionData rightVisionData = new VisionDataDO.VisionData().setNakedVision(rightNakedVision).setCorrectedVision(rightCorrectedVision).setGlassesType(glassesType).setLateriality(1);
        VisionDataDO visionDataDO = new VisionDataDO().setRightEyeData(rightVisionData).setLeftEyeData(leftVisionData);
        return visionScreeningResult.setVisionData(visionDataDO);
    }
}


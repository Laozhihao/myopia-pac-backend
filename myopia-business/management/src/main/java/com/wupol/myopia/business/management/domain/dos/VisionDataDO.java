package com.wupol.myopia.business.management.domain.dos;

import com.wupol.myopia.business.management.domain.dto.ScreeningResultBasicData;
import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description 视力筛查结果
 * @Date 2021/1/22 16:37
 * @Author by jacob
 */
@Data
@Accessors(chain = true)
public class VisionDataDO {

    /**
     * 右眼疾病
     */
    private VisionData rightEyeData;
    /**
     * 左眼疾病
     */
    private VisionData leftEyeData;


    @Data
    @Accessors(chain = true)
    public static class VisionData {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 佩戴眼镜的类型： @{link com.myopia.common.constant.WearingGlassesSituation}
         */
        private String glassesType;
        /**
         *  矫正视力
         */
        private BigDecimal correctedVision;
        /**
         * 裸眼视力
         */
        private BigDecimal nakedVision;
    }

}


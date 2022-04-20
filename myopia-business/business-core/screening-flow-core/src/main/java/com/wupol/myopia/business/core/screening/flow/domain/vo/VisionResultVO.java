package com.wupol.myopia.business.core.screening.flow.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author 钓猫的小鱼
 * @Date 2022/4/17 17:54
 * @Email: shuailong.wu@vistel.cn
 * @Des: 视力检查结果
 */
@Data
public class VisionResultVO {
    /**
     * 右眼视力
     */
    private VisionData rightEyeData;
    /**
     * 左眼视力
     */
    private VisionData leftEyeData;

    @Data
    public static class VisionData implements Serializable {

        /**
         * 裸眼视力
         */
        private BigDecimal nakedVision;
        /**
         * 裸眼视力-复测
         */
        private BigDecimal nakedVisionReScreening;
        /**
         * 裸眼视力-差值
         */
        private BigDecimal nakedVisionDeviation;

        /**
         * 矫正视力
         */
        private BigDecimal correctedVision;
        /**
         * 矫正视力-复测
         */
        private BigDecimal correctedVisionReScreening;
        /**
         * 矫正视力-差值
         */
        private BigDecimal correctedVisionDeviation;
    }
}

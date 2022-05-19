package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import lombok.Getter;
import lombok.Setter;

/**
 * 视力低下情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class LowMyopiaInfo {

    /**
     * 信息
     */
    private Info info;

    /**
     * 不同性别视力低下情况
     */
    private GenderLowVision genderLowVision;

    /**
     * 不同年级不同程度视力情况
     */
    private GradeLowVision gradeLowVision;

    /**
     * 不同年龄视力低下情况
     */
    private AgeLowVision ageLowVision;


    public static class Info {
        /**
         * 视力低下人数
         */
        private Integer lowVisionCount;

        /**
         * 视力低下率
         */
        private Integer lowVisionProportion;

        /**
         * 平均视力
         */
        private String avgVision;

        /**
         * 轻度视力低下
         */
        private CountAndProportion lightLowVision;

        /**
         * 中度视力低下
         */
        private CountAndProportion middleLowVision;

        /**
         * 重度视力低下
         */
        private CountAndProportion highLowVision;

    }
}

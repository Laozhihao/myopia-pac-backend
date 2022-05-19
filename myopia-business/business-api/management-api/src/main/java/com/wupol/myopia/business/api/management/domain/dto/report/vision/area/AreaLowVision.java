package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.VisionSituation;
import lombok.Getter;
import lombok.Setter;

/**
 * 视力低下
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AreaLowVision {

    /**
     * 信息
     */
    private Info info;

    /**
     * 不同性别视力低下
     */
    private GenderSexLowVision genderSexLowVision;

    /**
     * 不同学龄段不同程度视力情况
     */
    private SchoolAgeLowVision schoolAgeLowVision;

    /**
     * 不同年龄不同程度视力情况
     */
    private AgeLowVision ageLowVision;

    /**
     * 历年视力情况趋势分析
     */
    private LowVisionHistory lowVisionHistory;


    @Getter
    @Setter
    public static class Info {
        /**
         * 幼儿园
         */
        private Kindergarten kindergarten;

        /**
         * 小学
         */
        private Primary primary;
    }


    @Getter
    @Setter
    public static class Kindergarten {
        /**
         * 视力情况
         */
        private VisionSituation visionSituation;
    }

    @Getter
    @Setter
    public static class Primary {

        /**
         * 视力情况
         */
        private VisionSituation visionSituation;

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

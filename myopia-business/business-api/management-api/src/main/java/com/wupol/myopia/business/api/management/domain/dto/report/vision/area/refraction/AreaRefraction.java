package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary.PrimaryInfo;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import lombok.Getter;
import lombok.Setter;

/**
 * 屈光整体情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AreaRefraction {

    /**
     * 信息
     */
    private Info info;

    /**
     * 幼儿园
     */
    private KindergartenInfo kindergartenInfo;

    /**
     * 小学及以上
     */
    private PrimaryInfo primaryInfo;

    @Getter
    @Setter
    public static class Info {

        /**
         * 人数
         */
        private Integer count;

        /**
         * 幼儿园
         */
        private Kindergarten kindergarten;

        /**
         * 中小学及以上
         */
        private Primary primary;
    }


    @Getter
    @Setter
    public static class Kindergarten {

        /**
         * 幼儿园人数
         */
        private Integer count;

        /**
         * 远视储备不足
         */
        private CountAndProportion insufficientFarsightednessReserve;

        /**
         * 屈光不正
         */
        private CountAndProportion refractiveError;

        /**
         * 屈光参差
         */
        private CountAndProportion anisometropia;

    }

    @Getter
    @Setter
    public static class Primary {

        /**
         * 小学及以上人数
         */
        private Integer count;

        /**
         * 近视
         */
        private CountAndProportion myopia;

        /**
         * 近视前期
         */
        private CountAndProportion early;

        /**
         * 低度近视
         */
        private CountAndProportion lowMyopia;

        /**
         * 高度近视
         */
        private CountAndProportion highMyopia;

        /**
         * 佩戴角膜塑形镜
         */
        private CountAndProportion nightWearing;

        /**
         * 散光
         */
        private CountAndProportion astigmatism;

        /**
         * 戴镜
         */
        private CountAndProportion allGlasses;

        /**
         * 建议就诊
         */
        private CountAndProportion recommendDoctor;

        /**
         * 近视足矫
         */
        private CountAndProportion footOrthosis;

        /**
         * 近视未矫
         */
        private CountAndProportion uncorrected;

        /**
         * 近视欠矫
         */
        private CountAndProportion undercorrection;

    }


}

package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.PrimaryOverall;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 各学校整体情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolScreeningInfo {

    /**
     * 幼儿园
     */
    private Kindergarten kindergarten;

    /**
     * 小学以上
     */
    private PrimaryOverall primary;

    @Getter
    @Setter
    public static class Kindergarten {
        /**
         * 视力低常
         */
        private HighLowProportion lowVision;

        /**
         * 远视储备不足
         */
        private HighLowProportion insufficient;

        /**
         * 屈光不正
         */
        private HighLowProportion refractiveError;

        /**
         * 屈光参差
         */
        private HighLowProportion anisometropia;

        /**
         * 建议就诊
         */
        private HighLowProportion recommendDoctor;

        /**
         * 表格
         */
        private List<KindergartenScreeningInfoTable> tables;

    }

    @Getter
    @Setter
    public static class Table {

        /**
         * 学校名称
         */
        private String name;

        /**
         * 有效人数
         */
        private Integer validCount;

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

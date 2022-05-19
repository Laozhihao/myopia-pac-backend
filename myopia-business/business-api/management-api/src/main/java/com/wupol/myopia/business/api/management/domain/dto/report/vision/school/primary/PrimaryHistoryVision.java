package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import lombok.Getter;
import lombok.Setter;

/**
 * 历年视力情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PrimaryHistoryVision {

    /**
     * 信息
     */
    private Info info;

    /**
     * 表格
     */
    private Table tables;

    @Getter
    @Setter
    public static class Info {
        /**
         * 视力低下率
         */
        private String lowVision;

        /**
         * 近视率
         */
        private String myopia;

        /**
         * 近视前期率
         */
        private String early;

        /**
         * 低度近视率
         */
        private String lowMyopia;

        /**
         * 高度近视率
         */
        private String highMyopia;
    }

    @Getter
    @Setter
    public static class Table {

        /**
         * 项目 XX年
         */
        private String name;

        /**
         * 有效人数
         */
        private Integer validCount;

        /**
         * 视力低下人数
         */
        private Integer lowVision;

        /**
         * 视力低下人数-百分比
         */
        private Integer lowVisionProportion;

        /**
         * 近视人数
         */
        private Integer myopia;

        /**
         * 近视-百分比
         */
        private Integer myopiaProportion;

        /**
         * 视力前期人数
         */
        private Integer early;

        /**
         * 视力前期人数-百分比
         */
        private Integer earlyProportion;

        /**
         * 低度视力人数
         */
        private Integer lowMyopia;

        /**
         * 低度视力人数-百分比
         */
        private Integer lowMyopiaProportion;

        /**
         * 高度视力人数
         */
        private Integer highMyopia;

        /**
         * 高度视力人数-百分比
         */
        private Integer highMyopiaProportion;

    }

}

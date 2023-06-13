package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.myopia;

import lombok.Data;

import java.util.List;

/**
 * 小学及以上教育阶段不同性别近视情况
 *
 * @author Simple4H
 */
@Data
public class Module1 {

    /**
     * 表格
     */
    private List<TableItem> table;


    /**
     * 表格
     */
    @Data
    public static class TableItem {

        /**
         * 描述
         */
        private String desc;

        /**
         * 有效筛查人数
         */
        private Long validCount;

        /**
         * 近视人数
         */
        private Long myopiaCount;

        /**
         * 近视率
         */
        private String myopiaRadio;

        /**
         * 低度近视人数
         */
        private Long lightMyopiaCount;

        /**
         * 低度近视率
         */
        private String lightMyopiaRadio;

        /**
         * 低度近视占比
         */
        private String lightMyopiaProportion;

        /**
         * 高度近视人数
         */
        private String highMyopiaRadio;

        /**
         * 高度近视率
         */
        private Long highMyopiaCount;

        /**
         * 高度近视占比
         */
        private String highMyopiaProportion;

    }
}

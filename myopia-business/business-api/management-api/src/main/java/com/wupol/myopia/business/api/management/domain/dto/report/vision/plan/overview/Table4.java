package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.overview;

import lombok.Data;

import java.util.List;

/**
 * 小学及以上各教育阶段视力情况
 *
 * @author Simple4H
 */
@Data
public class Table4 {

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
         * 近视率
         */
        private String myopiaRadio;

        /**
         * 近视人数
         */
        private Long myopiaCount;

        /**
         * 视力不良率
         */
        private String lowVisionRadio;

        /**
         * 视力不良人数
         */
        private Long lowVisionCount;
    }

}

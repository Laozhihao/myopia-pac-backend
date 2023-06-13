package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.overview;

import lombok.Data;

import java.util.List;

/**
 * 各教育阶段视力监测预警
 *
 * @author Simple4H
 */
@Data
public class Table5 {

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
         * 建议就诊率
         */
        private String recommendDoctorRadio;

        /**
         * 建议就诊人数
         */
        private Long recommendDoctorCount;
    }
}

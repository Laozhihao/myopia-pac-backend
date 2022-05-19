package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage;

import lombok.Getter;
import lombok.Setter;

/**
 * 不同性别视力低下表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GenderSexLowVisionTable {

    /**
     * 性别
     */
    private String name;

    /**
     * 有效人数
     */
    private Integer validCount;

    /**
     * 幼儿园
     */
    private Table kTable;

    /**
     * 小学及以上
     */
    private Table pTable;

    @Getter
    @Setter
    public static class Table {

        /**
         * 人数
         */
        private Integer count;

        /**
         * 占比
         */
        private String proportion;

        /**
         * 平均视力
         */
        private String avgVision;
    }



}

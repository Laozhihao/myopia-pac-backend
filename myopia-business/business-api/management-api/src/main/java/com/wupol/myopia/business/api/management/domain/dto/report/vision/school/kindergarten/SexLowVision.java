package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同性别视力低下
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SexLowVision {

    /**
     * 详情
     */
    private Info info;

    /**
     * 表格
     */
    private List<Table> tables;


    @Getter
    @Setter
    public static class Info {
        /**
         * 视力低下
         */
        private String lowVisionPercentage;

        /**
         * 平均视力
         */
        private String avgVision;

        /**
         * 男生视力低下
         */
        private String mLowVisionPercentage;

        /**
         * 女视力低下
         */
        private String lLowVisionPercentage;
    }

    @Getter
    @Setter
    public static class Table {

        /**
         * 项目
         */
        private String name;

        /**
         * 有效人数
         */
        private Integer validCount;

        /**
         * 视力低下人数
         */
        private Integer lowVisionCount;

        /**
         * 视力低下占比
         */
        private String percentage;

        /**
         * 平均视力
         */
        private String avgVison;
    }


}

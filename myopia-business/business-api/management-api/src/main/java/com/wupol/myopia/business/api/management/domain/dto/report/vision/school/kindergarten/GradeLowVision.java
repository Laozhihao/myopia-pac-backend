package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同班级视力低下
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GradeLowVision {

    /**
     * 信息
     */
    private List<Info> infos;

    /**
     * 表格
     */
    private List<Table> tables;

    @Getter
    @Setter
    public static class Info {

        /**
         * 年级名称
         */
        private String name;

        /**
         * 视力低下率
         */
        private String lowVisionPercentage;

        /**
         * 最高班级
         */
        private String maxClass;

        /**
         * 最高百分比
         */
        private String maxPercentage;

        /**
         * 最低班级
         */
        private String minClass;

        /**
         * 最低百分比
         */
        private String minPercentage;
    }

    @Getter
    @Setter
    public static class Table {

        /**
         * 年级名称
         */
        private String name;

        /**
         * 班级名称
         */
        private String className;

        /**
         * 有效人数
         */
        private Integer validCount;

        /**
         * 男-人数
         */
        private Integer mCount;

        /**
         * 男-百分比
         */
        private String mPercentage;

        /**
         * 男-人数
         */
        private Integer fCount;

        /**
         * 女-百分比
         */
        private String fPercentage;


    }

}

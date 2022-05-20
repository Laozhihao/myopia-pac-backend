package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.MaxMinProportion;
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
    private Info info;

    /**
     * 表格
     */
    private List<Table> tables;

    @Getter
    @Setter
    public static class Info {

        /**
         * 小班
         */
        private MaxMinProportion one;

        /**
         * 中班
         */
        private MaxMinProportion two;

        /**
         * 大班
         */
        private MaxMinProportion three;
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
        private Long mCount;

        /**
         * 男-百分比
         */
        private String mProportion;

        /**
         * 男-人数
         */
        private Long fCount;

        /**
         * 女-百分比
         */
        private String fProportion;

        /**
         * 视力低下
         */
        private String lowVisionProportion;


    }

}

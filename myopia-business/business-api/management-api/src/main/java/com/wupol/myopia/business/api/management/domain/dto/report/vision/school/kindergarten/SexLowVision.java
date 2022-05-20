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
        private String lowVisionProportion;

        /**
         * 平均视力
         */
        private String avgVision;

        /**
         * 男生视力低下
         */
        private String mLowVisionProportion;

        /**
         * 女视力低下
         */
        private String lLowVisionProportion;
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
        private Long lowVisionCount;

        /**
         * 视力低下占比
         */
        private String proportion;

        /**
         * 平均视力
         */
        private String avgVision;
    }


}

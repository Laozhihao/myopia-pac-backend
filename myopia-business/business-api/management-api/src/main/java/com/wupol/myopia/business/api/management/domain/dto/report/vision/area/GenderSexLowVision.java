package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.GenderSexLowVisionTable;
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
public class GenderSexLowVision {

    /**
     * 信息
     */
    private Info info;

    /**
     * 不同性别视力低下表格
     */
    private List<GenderSexLowVisionTable> tables;


    /**
     * 信息
     */
    @Getter
    @Setter
    public static class Info {
        /**
         * 幼儿园
         */
        private Kindergarten kindergarten;

        /**
         * 小学
         */
        private Primary primary;

    }

    @Getter
    @Setter
    public static class Kindergarten {

        /**
         * 平均视力
         */
        private String avgVision;

        /**
         * 平均低常率
         */
        private String lowVisionProportion;

        /**
         * 男-平均低常率
         */
        private String mAvgVision;

        /**
         * 女-平均低常率
         */
        private String fAvgVision;
    }

    @Getter
    @Setter
    public static class Primary {
        /**
         * 平均视力
         */
        private String avgVision;

        /**
         * 平均低常率
         */
        private String lowVisionProportion;

        /**
         * 男-平均低常率
         */
        private String mAvgVision;

        /**
         * 女-平均低常率
         */
        private String fAvgVision;
    }
}

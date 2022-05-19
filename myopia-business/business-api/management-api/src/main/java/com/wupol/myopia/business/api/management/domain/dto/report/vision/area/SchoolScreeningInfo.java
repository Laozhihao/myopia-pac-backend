package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighAndLow;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 各学校整体情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolScreeningInfo {

    private Kindergarten kindergarten;

    private Primary primary;

    @Getter
    @Setter
    public static class Kindergarten {
        /**
         * 项目
         */
        private List<HighAndLow> info;

        /**
         * 表格
         */
        private List<KindergartenScreeningInfoTable> tables;

    }

    @Getter
    @Setter
    public static class Primary {
        /**
         * 项目
         */
        private List<HighAndLow> info;

        /**
         * 表格
         */
        private List<PrimaryScreeningInfoTable> tables;

    }

    @Getter
    @Setter
    public static class Table {

        /**
         * 学校名称
         */
        private String name;

        /**
         * 有效人数
         */
        private Integer validCount;

        /**
         * 近视
         */
        private CountAndProportion myopia;


        /**
         * 近视前期
         */
        private CountAndProportion early;

        /**
         * 低度近视
         */
        private CountAndProportion lowMyopia;

        /**
         * 高度近视
         */
        private CountAndProportion highMyopia;

        /**
         * 近视足矫
         */
        private CountAndProportion footOrthosis;

        /**
         * 近视未矫
         */
        private CountAndProportion uncorrected;

        /**
         * 近视欠矫
         */
        private CountAndProportion undercorrection;
    }
}

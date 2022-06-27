package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CommonWarningTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.MaxMinProportion;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年级班级视力预警情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GradeWarning {

    /**
     * 信息
     */
    private Info info;

    /**
     * 图表
     */
    private HorizontalChart gradeWarningChart;

    /**
     * 表格
     */
    private List<Table> tables;

    /**
     * 信息
     */
    @Getter
    @Setter
    public static class Info {

        /**
         * 小班
         */
        private Detail one;

        /**
         * 中班
         */
        private Detail two;

        /**
         * 大班
         */
        private Detail three;
    }

    /**
     * 信息
     */
    @Getter
    @Setter
    public static class Detail {

        /**
         * 预警率
         */
        private MaxMinProportion warningProportion;

        /**
         * 建议就诊率
         */
        private MaxMinProportion recommendDoctor;

        /**
         * 0级预警
         */
        private String zeroWarning;

        /**
         * 1级预警
         */
        private String oneWarning;

        /**
         * 2级预警
         */
        private String twoWarning;

        /**
         * 3级预警
         */
        private String threeWarning;
    }

    /**
     * 表格
     */
    @Getter
    @Setter
    public static class Table extends CommonWarningTable {

        /**
         * 预警人数
         */
        private Long warningCount;

        /**
         * 预警人数百分比
         */
        private String warningProportion;
    }


}

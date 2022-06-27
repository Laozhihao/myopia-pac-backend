package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.PrimaryScreeningInfoTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 小学及以上整体情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PrimaryOverall {

    /**
     * 信息
     */
    private Info info;

    private List<StackedChart> charts;

    /**
     * 表格
     */
    private List<PrimaryScreeningInfoTable> tables;



    @Getter
    @Setter
    public static class Info {
        /**
         * 视力低下
         */
        private MaxMinProportion lowVision;

        /**
         * 近视
         */
        private MaxMinProportion myopia;

        /**
         * 近视前期
         */
        private MaxMinProportion earlyMyopia;

        /**
         * 低度近视
         */
        private MaxMinProportion lightMyopia;

        /**
         * 高度近视
         */
        private MaxMinProportion highMyopia;

        /**
         * 建议就诊
         */
        private MaxMinProportion recommendDoctor;

        /**
         * 欠矫未矫
         */
        private MaxMinProportion owe;
    }
}

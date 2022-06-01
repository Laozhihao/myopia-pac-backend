package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 历年屈光情况趋势分析
 *
 * @author Simple4H
 */
@Getter
@Setter
public class HistoryRefraction {

    /**
     * 信息
     */
    private Info info;

    /**
     * 历年屈光情况
     */
    private HorizontalChart primaryHistoryRefraction;

    /**
     * 表格
     */
    private List<AstigmatismTable> tables;

    @Getter
    @Setter
    public static class Info {
        /**
         * 近视率
         */
        private String myopiaProportion;

        /**
         * 近视前期率
         */
        private String earlyMyopiaProportion;

        /**
         * 低度近视率
         */
        private String lightMyopiaProportion;

        /**
         * 高度近视率
         */
        private String highMyopiaProportion;
    }


}

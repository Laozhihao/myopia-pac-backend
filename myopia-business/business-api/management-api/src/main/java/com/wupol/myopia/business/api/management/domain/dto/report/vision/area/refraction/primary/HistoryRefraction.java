package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AstigmatismTable;
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
     * 表格
     */
    private List<AstigmatismTable> tables;

    @Getter
    @Setter
    public static class Info {
        /**
         * 近视率
         */
        private Integer myopiaProportion;

        /**
         * 近视前期率
         */
        private Integer earlyMyopiaProportion;


        /**
         * 低度近视率
         */
        private Integer lowMyopiaProportion;


        /**
         * 高度近视率
         */
        private Integer highMyopiaProportion;
    }


}

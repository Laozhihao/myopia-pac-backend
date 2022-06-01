package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.MyopiaTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 历年视力情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PrimaryHistoryVision {

    /**
     * 信息
     */
    private Info info;

    /**
     * 图表
     */
    private HorizontalChart primaryHistoryVisionChart;

    /**
     * 等级图表
     */
    private HorizontalChart primaryLevelHistoryVisionChart;

    /**
     * 表格
     */
    private List<MyopiaTable> tables;

    @Getter
    @Setter
    public static class Info {
        /**
         * 视力低下率
         */
        private String lowVision;

        /**
         * 近视率
         */
        private String myopia;

        /**
         * 近视前期率
         */
        private String early;

        /**
         * 低度近视率
         */
        private String lightMyopia;

        /**
         * 高度近视率
         */
        private String highMyopia;
    }

}

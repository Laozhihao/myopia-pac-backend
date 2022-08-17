package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.PieChart;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同性别视力低下情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GenderLowVision {

    /**
     * 信息
     */
    private Info info;

    /**
     * 性别视力低下饼图
     */
    private PieChart genderLowVisionChart;

    /**
     * 视力低下饼图
     */
    private PieChart lowVisionChart;

    /**
     * 表格
     */
    private List<CommonLowVisionTable> tables;

    /**
     * 信息
     */
    @Getter
    @Setter
    public static class Info {

        /**
         * 平均视力
         */
        private String avgVision;

        /**
         * 视力低下率
         */
        private String lowVisionProportion;

        /**
         * 轻
         */
        private String lightLowVision;

        /**
         * 中
         */
        private String middleLowVision;

        /**
         * 高
         */
        private String highLowVision;

        /**
         * 男-视力低下
         */
        private String mLowVision;

        /**
         * 女-视力低下
         */
        private String fLowVision;
    }
}

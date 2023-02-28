package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.CommonLowVisionTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同学龄段不同程度视力情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolAgeLowVision {

    /**
     * 信息
     */
    private Info info;

    /**
     * 视力低下表格
     */
    private HorizontalChart lowVisionChart;

    /**
     * 视力低下程度表格
     */
    private HorizontalChart lowVisionLevelChart;

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
         * 幼儿园视力低下率
         */
        private String kindergartenLowVisionProportion;

        /**
         * 小学视力低下率
         */
        private String primaryLowVisionProportion;

        /**
         * 初中视力低下率
         */
        private String juniorLowVisionProportion;

        /**
         * 高中
         */
        private String seniorLowVisionProportion;

        /**
         * 普高视力低下率
         */
        private String highLowVisionProportion;

        /**
         * 职高视力低下率
         */
        private String vocationalLowVisionProportion;

        /**
         * 小学以上不同程度
         */
        private Detail detail;

    }

    @Getter
    @Setter
    public static class Detail {
        /**
         * 轻度视力低下率
         */
        private HighLowProportion light;

        /**
         * 中度视力低下率
         */
        private HighLowProportion middle;

        /**
         * 重度视力低下率
         */
        private HighLowProportion high;
    }

}

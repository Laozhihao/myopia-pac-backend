package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.HighLowProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.SchoolAgeLowVisionTable;
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
     * 表格
     */
    private List<SchoolAgeLowVisionTable> table;

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
         * 高中视力低下率
         */
        private String highLowVisionProportion;

        /**
         * 普高视力低下率
         */
        private String puGaoLowVisionProportion;

        /**
         * 职高视力低下率
         */
        private String vocationalLowVisionProportion;

        /**
         * 轻度视力低下率
         */
        private HighLowProportion low;

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

package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AgeWearingTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年龄段近视矫正情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AgeWearingGlasses {

    /**
     * 年龄段
     */
    private String ageInfo;

    /**
     * 信息
     */
    private Info info;

    /**
     * 表格
     */
    private HorizontalChart ageWearingGlassesChart;

    /**
     * 表格
     */
    private HorizontalChart ageVisionCorrectionChart;

    /**
     * 表格
     */
    private List<AgeWearingTable> tables;

    @Getter
    @Setter
    public static class Info {
        /**
         * 不佩戴眼镜
         */
        private HighLowProportion notWearing;

        /**
         * 佩戴框架眼镜
         */
        private HighLowProportion glasses;

        /**
         * 佩戴隐形眼镜
         */
        private HighLowProportion contact;

        /**
         * 夜戴
         */
        private HighLowProportion night;

        /**
         * 足矫
         */
        private HighLowProportion enough;

        /**
         * 未矫
         */
        private HighLowProportion uncorrected;

        /**
         * 欠矫
         */
        private HighLowProportion under;
    }

}

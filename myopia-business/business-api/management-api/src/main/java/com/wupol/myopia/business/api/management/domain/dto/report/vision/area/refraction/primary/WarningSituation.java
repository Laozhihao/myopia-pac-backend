package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.PortraitChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.VisionWarningSituation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 近视预警情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class WarningSituation {

    /**
     * 近视预警情况
     */
    private VisionWarningSituation visionWarningSituation;

    /**
     * 建议就诊
     */
    private CountAndProportion recommendDoctor;

    /**
     * 不同年级近视预警情况
     */
    private GradeWarningInfo gradeWarningInfo;

    @Getter
    @Setter
    public static class GradeWarningInfo {

        /**
         * 信息
         */
        private Info info;

        /**
         * 图表
         */
        private PortraitChart gradeWarningChart;

        /**
         * 表格
         */
        private List<WarningTable> tables;
    }


    @Getter
    @Setter
    public static class Info {
        /**
         * 预警等级
         */
        private HighLowProportion zero;

        /**
         * 预警等级
         */
        private HighLowProportion one;

        /**
         * 预警等级
         */
        private HighLowProportion two;

        /**
         * 预警等级
         */
        private HighLowProportion three;

        /**
         * 建议就诊
         */
        private HighLowProportion recommendDoctor;
    }


}

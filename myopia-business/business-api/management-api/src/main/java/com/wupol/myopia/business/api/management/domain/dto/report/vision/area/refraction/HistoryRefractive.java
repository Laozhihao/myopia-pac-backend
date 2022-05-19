package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
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
public class HistoryRefractive {

    /**
     * 信息
     */
    private Info info;

    /**
     * 表格
     */
    private List<RefractiveTable> tables;

    @Getter
    @Setter
    public static class Info {
        /**
         * 远视储备不足
         */
        private Integer insufficientPercentage;

        /**
         * 屈光不正率
         */
        private Integer refractiveErrorPercentage;

        /**
         * 屈光参差率
         */
        private Integer anisometropiaPercentage;
    }
}

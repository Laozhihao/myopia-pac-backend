package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.GenderProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.PortraitChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同性别近视（散光）情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GenderAstigmatism {

    /**
     * 信息
     */
    private Info info;

    /**
     * 表格
     */
    private HorizontalChart genderAstigmatismChart;

    /**
     * 表格
     */
    private List<AstigmatismTable> tables;

    @Getter
    @Setter
    public static class Info {
        /**
         * 近视
         */
        private GenderProportion myopia;

        /**
         * 近视前期
         */
        private GenderProportion earlyMyopia;

        /**
         * 低度近视
         */
        private GenderProportion lightMyopia;

        /**
         * 高度近视
         */
        private GenderProportion highMyopia;

        /**
         * 散光
         */
        private GenderProportion astigmatism;
    }
}

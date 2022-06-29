package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同学龄段屈光筛查情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolAgeRefractive {

    /**
     * 信息
     */
    public Info info;

    /**
     * 学龄段图表
     */
    private HorizontalChart ageRefractiveChart;

    /**
     * 表格
     */
    private List<RefractiveTable> tables;

    /**
     * 信息
     */
    @Getter
    @Setter
    public static class Info {

        /**
         * 一年级
         */
        private Detail one;

        /**
         * 二年级
         */
        private Detail two;

        /**
         * 三年级
         */
        private Detail three;

    }

    @Getter
    @Setter
    public static class Detail {
        /**
         * 远视储备不足率
         */
        private String insufficient;
        /**
         * 屈光不正率
         */
        private String refractiveError;
        /**
         * 屈光参差率
         */
        private String anisometropia;
    }


}

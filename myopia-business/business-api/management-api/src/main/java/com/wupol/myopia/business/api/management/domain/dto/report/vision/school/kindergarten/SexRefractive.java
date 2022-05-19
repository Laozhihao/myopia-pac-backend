package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同性别屈光筛查情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SexRefractive {

    /**
     * 信息
     */
    private List<Info> info;

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
         * 百分比
         */
        private String percentage;

        /**
         * 男百分比
         */
        private String mPercentage;

        /**
         * 女百分比
         */
        private String fPercentage;
    }

}

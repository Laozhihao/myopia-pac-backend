package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年龄段屈光筛查情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AgeRefraction {

    /**
     * 年龄段
     */
    private String ageRange;

    /**
     * 近视前期
     */
    private HighLowProportion earlyMyopia;

    /**
     * 近视
     */
    private HighLowProportion myopia;

    /**
     * 散光
     */
    private HighLowProportion astigmatism;

    /**
     * 低度近视
     */
    private HighLowProportion lightMyopia;

    /**
     * 高度近视
     */
    private HighLowProportion highMyopia;

    /**
     * 图表
     */
    private HorizontalChart ageRefractionChart;

    /**
     * 等级图表
     */
    private HorizontalChart levelAgeRefractionChart;

    /**
     * 表格
     */
    private List<AstigmatismTable> tables;
}

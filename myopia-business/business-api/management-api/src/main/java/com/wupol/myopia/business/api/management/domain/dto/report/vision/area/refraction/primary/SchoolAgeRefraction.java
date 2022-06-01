package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.PortraitChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
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
public class SchoolAgeRefraction {

    /**
     * 近视
     */
    private HighLowProportion myopia;

    /**
     * 散光
     */
    private HighLowProportion astigmatism;

    /**
     * 近视前期
     */
    private HighLowProportion earlyMyopia;

    /**
     * 低度近视
     */
    private HighLowProportion lightMyopia;

    /**
     * 高度近视
     */
    private HighLowProportion highMyopia;

    /**
     * 不同学龄屈光表格
     */
    private PortraitChart schoolAgeRefractionChart;

    /**
     * 表格
     */
    private List<AstigmatismTable> tables;


}

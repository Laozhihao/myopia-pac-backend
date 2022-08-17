package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.GenderProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 男女屈光筛查情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GenderRefraction {

    /**
     * 近视
     */
    private GenderProportion myopia;

    /**
     * 散光
     */
    private GenderProportion astigmatism;

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
     * 男女屈光图表
     */
    private HorizontalChart genderRefractionChart;

    /**
     * 表格
     */
    private List<AstigmatismTable> tables;
}

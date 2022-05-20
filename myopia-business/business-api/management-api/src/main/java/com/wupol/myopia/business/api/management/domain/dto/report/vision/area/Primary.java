package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.VisionSituation;
import lombok.Getter;
import lombok.Setter;

/**
 * 中小学
 *
 * @author Simple4H
 */
@Getter
@Setter
public class Primary {

    /**
     * 视力情况
     */
    private VisionSituation visionSituation;

    /**
     * 近视
     */
    private CountAndProportion myopia;

    /**
     * 小学
     */
    private CountAndProportion primaryProportion;

    /**
     * 初中
     */
    private CountAndProportion juniorProportion;

    /**
     * 高中
     */
    private CountAndProportion highProportion;

    /**
     * 职业高中
     */
    private CountAndProportion vocationalHighProportion;

    /**
     * 近视前期
     */
    private CountAndProportion early;

    /**
     * 低度近视
     */
    private CountAndProportion lowMyopia;

    /**
     * 高度近视
     */
    private CountAndProportion highMyopia;

    /**
     * 近视足矫
     */
    private CountAndProportion footOrthosis;

    /**
     * 近视未矫
     */
    private CountAndProportion uncorrected;

    /**
     * 近视欠矫
     */
    private CountAndProportion undercorrection;
}

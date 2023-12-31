package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import lombok.Getter;
import lombok.Setter;

/**
 * 屈光情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class RefractionSituation {

    /**
     * 近视
     */
    private CountAndProportion myopia;

    /**
     * 佩戴角膜塑形镜
     */
    private CountAndProportion night;

    /**
     * 近视前期
     */
    private CountAndProportion early;

    /**
     * 低度近视
     */
    private CountAndProportion light;

    /**
     * 高度近视
     */
    private CountAndProportion high;

    /**
     * 散光
     */
    private CountAndProportion astigmatism;

    /**
     * 近视足矫
     */
    private CountAndProportion enough;

    /**
     * 近视未矫
     */
    private CountAndProportion uncorrected;

    /**
     * 近视欠矫
     */
    private CountAndProportion under;


}

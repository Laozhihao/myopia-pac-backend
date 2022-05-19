package com.wupol.myopia.business.api.management.domain.dto.report.vision.area;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
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
     * 近视
     */
    private CountAndProportion myopia;

    /**
     * 近视详情
     */
    private MyopiaInfo myopiaInfo;

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

    @Getter
    @Setter
    public static class MyopiaInfo {

        /**
         * 学龄
         */
        private String schoolAge;

        /**
         * 占比
         */
        private String Proportion;
    }
}

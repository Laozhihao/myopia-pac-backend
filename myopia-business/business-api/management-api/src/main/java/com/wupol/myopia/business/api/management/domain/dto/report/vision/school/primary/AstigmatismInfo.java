package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import lombok.Getter;
import lombok.Setter;

/**
 * 散光情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AstigmatismInfo {

    /**
     * 信息
     */
    private Info info;

    /**
     * 不同性别近视（散光）情况
     */
    private GenderAstigmatism genderAstigmatism;

    /**
     * 不同年级近视（散光）情况
     */
    private GradeAstigmatism gradeAstigmatism;

    /**
     * 不同年龄近视（散光）情况
     */
    private AgeAstigmatism ageAstigmatism;

    @Getter
    @Setter
    public static class Info {

        /**
         * 近视
         */
        private CountAndProportion myopia;

        /**
         * 近视前期
         */
        private CountAndProportion earlyMyopia;

        /**
         * 低度近视
         */
        private CountAndProportion lightMyopia;

        /**
         * 高度近视
         */
        private CountAndProportion highMyopia;

        /**
         * 佩戴角膜塑形镜
         */
        private CountAndProportion nightWearing;

        /**
         * 散光
         */
        private CountAndProportion astigmatism;

    }
}

package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import lombok.Getter;
import lombok.Setter;

/**
 * 矫正戴镜情况
 *
 * @author Simple4H
 */

@Getter
@Setter
public class WearingGlassesInfo {

    /**
     * 信息
     */
    private Info info;

    /**
     * 不同性别矫正戴镜情况情况
     */
    private GenderWearingGlasses genderWearingGlasses;

    /**
     * 不同年级矫正戴镜情况情况
     */
    private GradeWearingGlasses gradeWearingGlasses;

    /**
     * 不同年龄矫正戴镜情况情况
     */
    private AgeWearingGlasses ageWearingGlasses;


    @Getter
    @Setter
    private static class Info {

        /**
         * 矫正人数
         */
        private Integer correctionCount;

        /**
         * 不佩戴眼镜
         */
        private CountAndProportion notWearing;

        /**
         * 佩戴框架眼镜
         */
        private CountAndProportion wearingFrame;

        /**
         * 佩戴隐形眼镜
         */
        private CountAndProportion wearingContact;

        /**
         * 夜戴
         */
        private CountAndProportion nightWearing;

        /**
         * 足矫
         */
        private CountAndProportion enough;

        /**
         * 未矫
         */
        private CountAndProportion notYet;

        /**
         * 欠矫
         */
        private CountAndProportion owe;
    }
}

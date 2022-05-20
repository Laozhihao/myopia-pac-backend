package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.GenderProportion;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同性别矫正戴镜情况情况
 *
 * @author Simple4H
 */

@Getter
@Setter
public class GenderWearingGlasses {

    /**
     * 信息
     */
    private Info info;

    /**
     * 表格
     */
    private List<AgeWearingTable> tables;

    @Getter
    @Setter
    public static class Info {
        /**
         * 不戴镜
         */
        private GenderProportion notWearing;

        /**
         * 框架眼镜
         */
        private GenderProportion glasses;

        /**
         * 隐形眼镜
         */
        private GenderProportion contact;

        /**
         * 夜戴角膜塑形镜
         */
        private GenderProportion night;

        /**
         * 足矫
         */
        private GenderProportion enough;

        /**
         * 欠矫
         */
        private GenderProportion under;

        /**
         * 未矫
         */
        private GenderProportion uncorrected;
    }

}

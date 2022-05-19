package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
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
public class SchoolAgeRefractive {

    /**
     * 信息
     */
    public Info info;

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
         * 远视储备不足率
         */
        private String oneInsufficient;
        /**
         * 远视储备不足率
         */
        private String twoInsufficient;
        /**
         * 远视储备不足率
         */
        private String threeInsufficient;


        /**
         * 屈光不正率
         */
        private String oneRefractiveError;
        /**
         * 屈光不正率
         */
        private String twoRefractiveError;
        /**
         * 屈光不正率
         */
        private String threeRefractiveError;

        /**
         * 屈光参差率
         */
        private String oneAnisometropia;
        /**
         * 屈光参差率
         */
        private String twoAnisometropia;
        /**
         * 屈光参差率
         */
        private String threeAnisometropia;
    }


}

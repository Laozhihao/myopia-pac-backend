package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 各班筛查数据
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ClassScreeningData {

    private List<ClassScreeningDataDetail> abc;

    @Getter
    @Setter
    public static class ClassScreeningDataDetail {

        /**
         * 姓名
         */
        private String name;

        /**
         * 性别
         */
        private String gender;

        /**
         * 戴镜情况
         */
        private String glassesType;

        /**
         * 裸眼视力
         */
        private String nakedVision;

        /**
         * 矫正视力
         */
        private String correctedVision;

        /**
         * 球镜
         */
        private String sph;

        /**
         * 柱镜
         */
        private String cyl;

        /**
         * 轴位
         */
        private String axsi;

        /**
         * 等效球镜
         */
        private String se;

        /**
         * 视力分析
         */
        private String visionInfo;

        /**
         * 屈光分析
         */
        private String refractiveInfo;

        /**
         * 视力预警
         */
        private String visionWarning;

        /**
         * 建议就诊
         */
        private Boolean isRecommendDoctor;
    }

}



package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同年级班级视力预警情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GradeWarning {

    /**
     * 信息
     */
    private List<Info> info;

    /**
     * 表格
     */
    private List<Table> tables;

    /**
     * 信息
     */
    @Getter
    @Setter
    public static class Info {

        /**
         * 班级名称
         */
        private String name;

        /**
         * 预警率
         */
        private String warningPercentage;

        /**
         * 预警率最高班级
         */
        private String maxClassNameWarning;

        /**
         * 预警率最高班级 百分比
         */
        private String maxClassWarningPercentage;

        /**
         * 预警率最低班级
         */
        private String minClassNameWarning;

        /**
         * 预警率最低班级 百分比
         */
        private String minClassWarningPercentage;

        /**
         * 0级预警
         */
        private String zeroWarning;

        /**
         * 1级预警
         */
        private String oneWarning;

        /**
         * 2级预警
         */
        private String twoWarning;

        /**
         * 3级预警
         */
        private String threeWarning;

        /**
         * 建议就诊率
         */
        private String recommendDoctorPercentage;

        /**
         * 最高建议就诊率-班级
         */
        private String maxClassNameRecommendDoctor;

        /**
         * 最高建议就诊率-班级百分比
         */
        private String maxClassWarningRecommendDoctorPercentage;

        /**
         * 最低建议就诊率-班级
         */
        private String minClassNameRecommendDoctor;

        /**
         * 最低建议就诊率-班级百分比
         */
        private String minClassRecommendDoctorPercentage;


    }

    /**
     * 表格
     */
    @Getter
    @Setter
    public static class Table {

        /**
         * 年级名称
         */
        private String name;

        /**
         * 班级名称
         */
        private String className;

        /**
         * 有效人数
         */
        private Integer validCount;

        /**
         * 0级预警人数
         */
        private Integer zeroWarningCount;

        /**
         * 0级预警人数百分比
         */
        private String zeroWarningPercentage;

        /**
         * 1级预警人数
         */
        private Integer oneWarningCount;

        /**
         * 1级预警人数百分比
         */
        private String oneWarningPercentage;

        /**
         * 2级预警人数
         */
        private Integer twoWarningCount;

        /**
         * 2级预警人数百分比
         */
        private String twoWarningPercentage;

        /**
         * 3级预警人数
         */
        private Integer threeWarningCount;

        /**
         * 3级预警人数百分比
         */
        private String threeWarningPercentage;

        /**
         * 建议就诊人数
         */
        private Integer recommendDoctorCount;

        /**
         * 建议就诊百分比
         */
        private String recommendDoctorPercentage;
    }


}

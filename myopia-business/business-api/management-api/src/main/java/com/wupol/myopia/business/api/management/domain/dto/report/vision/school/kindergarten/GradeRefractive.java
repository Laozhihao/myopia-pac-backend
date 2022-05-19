package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 不同班级屈光
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GradeRefractive {

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
         * 远视储备不足不足率
         */
        private Integer insufficientPercentage;

        /**
         * 远视储备不足率最高-班级名称
         */
        private String maxClassNameInsufficient;

        /**
         * 远视储备不足率最高-百分比
         */
        private Integer maxInsufficientPercentage;

        /**
         * 屈光不正率最高-班级名称
         */
        private String maxClassNameRefractiveError;

        /**
         * 屈光不正率最高-百分比
         */
        private Integer maxRefractiveErrorPercentage;

        /**
         * 屈光参差率最高-班级名称
         */
        private String maxClassNameAnisometropia;

        /**
         * 屈光参差率最高-百分比
         */
        private Integer maxAnisometropiaPercentage;
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
         * 远视储备不足-有效人数
         */
        private Integer insufficientStudentCount;

        /**
         * 远视储备不足-百分比
         */
        private Integer insufficientPercentage;

        /**
         * 屈光不正-有效人数
         */
        private Integer refractiveErrorStudentCount;

        /**
         * 屈光不正-百分比
         */
        private Integer refractiveErrorPercentage;

        /**
         * 屈光参差-有效人数
         */
        private Integer anisometropiaStudentCount;

        /**
         * 屈光参差-百分比
         */
        private Integer anisometropiaPercentage;
    }

}

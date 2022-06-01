package com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.MaxMinProportion;
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
    private Info info;

    /**
     * 图表
     */
    private HorizontalChart gradeRefractiveChart;

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
         * 小班
         */
        private Detail one;

        /**
         * 中班
         */
        private Detail two;

        /**
         * 大班
         */
        private Detail three;

    }

    /**
     * 信息
     */
    @Getter
    @Setter
    public static class Detail {

        /**
         * 远视储备不足
         */
        private MaxMinProportion insufficient;

        /**
         * 屈光不正率
         */
        private MaxMinProportion refractiveError;

        /**
         * 屈光参差率最高最高-班级名称
         */
        private MaxMinProportion anisometropia;
    }


    /**
     * 表格
     */
    @Getter
    @Setter
    public static class Table extends RowSpan{

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
        private Long insufficientStudentCount;

        /**
         * 远视储备不足-百分比
         */
        private String insufficientProportion;

        /**
         * 屈光不正-有效人数
         */
        private Long refractiveErrorStudentCount;

        /**
         * 屈光不正-百分比
         */
        private String refractiveErrorProportion;

        /**
         * 屈光参差-有效人数
         */
        private Long anisometropiaStudentCount;

        /**
         * 屈光参差-百分比
         */
        private String anisometropiaProportion;
    }

}

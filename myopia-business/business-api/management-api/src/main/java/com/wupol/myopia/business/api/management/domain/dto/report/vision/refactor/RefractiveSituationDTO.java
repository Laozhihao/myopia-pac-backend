package com.wupol.myopia.business.api.management.domain.dto.report.vision.refactor;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 屈光情况
 *
 * @author Simple4H
 */
@Getter
@Setter
public class RefractiveSituationDTO {

    /**
     * 信息
     */
    private RefractiveSituationInfo refractiveSituationInfo;

    /**
     * 不同性别屈光情况
     */
    private GenderRefractiveSituation genderRefractiveSituation;

    /**
     * 不同年级屈光情况
     */
    private GradeRefractiveSituation gradeRefractiveSituation;

    /**
     * 不同班级屈光情况/欠矫
     */
    private ClassRefractiveSituation classRefractiveSituation;

    /**
     * 屈光情况信息
     */
    @Getter
    @Setter
    public static class RefractiveSituationInfo extends RefractiveSituation {

        /**
         * 屈光不正人数
         */
        private Long refractiveErrorNum;
    }

    /**
     * 不同性别屈光情况
     */
    @Getter
    @Setter
    public static class GenderRefractiveSituation {

        private List<RefractiveSituation> items;

        /**
         * 屈光整体情况
         */
        private List<PieChart> table1;

        /**
         * 男生屈光整体情况
         */
        private List<PieChart> table2;

        /**
         * 女生
         */
        private List<PieChart> table3;

    }

    /**
     * 不同年级屈光情况
     */
    @Getter
    @Setter
    public static class GradeRefractiveSituation {

        /**
         * 详情
         */
        private List<GradeRefractiveSituationItem> items;

        /**
         * 表格
         */
        private Object table1;

        /**
         * 表格
         */
        private Object table2;

        /**
         * 表格
         */
        private Object table3;

        /**
         * 总结
         */
        private List<GradeRefractiveSituationSummary> summary;

    }

    /**
     * 不同年级屈光情况
     */
    @Getter
    @Setter
    public static class GradeRefractiveSituationItem extends RefractiveSituation {

        /**
         * 年级名称
         */
        private String gradeName;
    }

    /**
     * 不同班级屈光情况
     */
    @Getter
    @Setter
    public static class ClassRefractiveSituation {

        /**
         * 详情
         */
        private List<ClassRefractiveSituationItem> items;
    }

    /**
     * 不同班级屈光情况
     */
    @Getter
    @Setter
    public static class ClassRefractiveSituationItem extends RefractiveSituation {

        /**
         * 年级名称
         */
        private String gradeName;

        /**
         * 班级名称
         */
        private String className;

        /**
         * rowSpan
         */
        private Integer rowSpan;
    }

    /**
     * 总结
     */
    @Getter
    @Setter
    public static class GradeRefractiveSituationSummary {

        /**
         * 名称
         */
        private String keyName;

        /**
         * 最高年级名称
         */
        private List<String> gradeNameHigh;

        /**
         * 最高百分比
         */
        private String radioHigh;

        /**
         * 最低年级名称
         */
        private List<String> gradeNameLow;

        /**
         * 最低百分比
         */
        private String radioLow;
    }

    /**
     * 屈光情况
     */
    @Getter
    @Setter
    public static class RefractiveSituation {

        /**
         * 筛查学生
         */
        private Long screeningStudentNum;

        /**
         * 低度近视人数
         */
        private Long lowMyopiaNum;

        /**
         * 低度近视百分比
         */
        private String lowMyopiaRatio;

        /**
         * 高度近视人数
         */
        private Long highMyopiaNum;

        /**
         * 高度近视百分比
         */
        private String highMyopiaRatio;

        /**
         * 散光人数
         */
        private Long astigmatismNum;

        /**
         * 散光百分比
         */
        private String astigmatismRatio;
    }

}

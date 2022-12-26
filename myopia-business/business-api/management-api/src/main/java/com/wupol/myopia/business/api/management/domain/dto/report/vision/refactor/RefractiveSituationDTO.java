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

    @Getter
    @Setter
    public static class GenderRefractiveSituation {

        private List<RefractiveSituation> items;

        private Object table1;
        private Object table2;
        private Object table3;

    }

    @Getter
    @Setter
    public static class GradeRefractiveSituation {

        private List<GradeRefractiveSituationItem> items;

        private Object table1;
        private Object table2;
        private Object table3;

        private Object summary;

    }

    @Getter
    @Setter
    public static class GradeRefractiveSituationItem extends RefractiveSituation {

        /**
         * 年级名称
         */
        private String gradeName;
    }

    @Getter
    @Setter
    public static class ClassRefractiveSituation {
        private List<ClassRefractiveSituationItem> items;
    }

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
    }


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

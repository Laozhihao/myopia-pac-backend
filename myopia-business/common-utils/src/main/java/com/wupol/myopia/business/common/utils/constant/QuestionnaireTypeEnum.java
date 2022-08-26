package com.wupol.myopia.business.common.utils.constant;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 问卷类型Enum
 *
 * @author Simple4H
 */
@Getter
public enum QuestionnaireTypeEnum {
    QUESTIONNAIRE_NOTICE(0, "问卷填写引导、基本资料(学生)"),
    AREA_DISTRICT_SCHOOL(1, "省、地市及区（县）管理部门学校卫生工作调查表（各级卫生行政部门人员填写）"),
    PRIMARY_SECONDARY_SCHOOLS(2, "中小学校开展学校卫生工作情况调查表"),
    PRIMARY_SCHOOL(3, "学生健康状况及影响因素调查表（小学版）"),
    MIDDLE_SCHOOL(4, "学生健康状况及影响因素调查表（中学版）"),
    UNIVERSITY_SCHOOL(5, "学生健康状况及影响因素调查表（大学版）"),
    VISION_SPINE(6, "学生视力不良及脊柱弯曲异常影响因素专项调查表"),
    SCHOOL_ENVIRONMENT(7, "学校环境健康影响因素调查表（疾控部门填写）"),

    VISION_SPINE_NOTICE(8, "脊柱弯曲-个人信息"),

    ARCHIVE_REC(9, "学生重点常见病监测表");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 描述
     **/
    private final String desc;

    QuestionnaireTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static List<QuestionnaireTypeEnum> getPrimaryType() {
        return Lists.newArrayList(QUESTIONNAIRE_NOTICE, VISION_SPINE, PRIMARY_SCHOOL);
    }

    public static List<QuestionnaireTypeEnum> getMiddleType() {
        return Lists.newArrayList(QUESTIONNAIRE_NOTICE, VISION_SPINE, MIDDLE_SCHOOL);
    }

    public static List<QuestionnaireTypeEnum> getUniversityType() {
        return Lists.newArrayList(QUESTIONNAIRE_NOTICE, VISION_SPINE, UNIVERSITY_SCHOOL);
    }

    /**
     * 通过学龄段获取报告
     *
     * @param gradeType 学龄段
     *
     * @return List<QuestionnaireTypeEnum>
     */
    public static List<QuestionnaireTypeEnum> getStudentQuestionnaireBySchoolAge(Integer gradeType) {
        if (Boolean.TRUE.equals(SchoolAge.isPrimary(gradeType))) {
            return getPrimaryType();
        }
        if (Boolean.TRUE.equals(SchoolAge.isMiddleSchool(gradeType))) {
            return getMiddleType();
        }
        if (Boolean.TRUE.equals(SchoolAge.isUniversity(gradeType))) {
            return getUniversityType();
        }
        return new ArrayList<>();
    }

    public static QuestionnaireTypeEnum getQuestionnaireType(Integer type) {
        return Arrays.stream(values())
                .filter(item -> Objects.equals(item.getType(), type))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取学校问卷
     *
     * @return List<QuestionnaireTypeEnum>
     */
    public static List<QuestionnaireTypeEnum> getSchoolQuestionnaireType() {
        return Lists.newArrayList(PRIMARY_SECONDARY_SCHOOLS);
    }

    /**
     * 获取政府问卷
     *
     * @return List<QuestionnaireTypeEnum>
     */
    public static List<QuestionnaireTypeEnum> getGovQuestionnaireType() {
        return Lists.newArrayList(SCHOOL_ENVIRONMENT, AREA_DISTRICT_SCHOOL);
    }

}

package com.wupol.myopia.business.common.utils.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 问卷类型Enum
 *
 * @author Simple4H
 */
@Getter
public enum QuestionnaireTypeEnum {

    AREA_DISTRICT_SCHOOL(1, "省、地市及区（县）管理部门学校卫生工作调查表"),
    PRIMARY_SECONDARY_SCHOOLS(2, "中小学校开展学校卫生工作情况调查表"),
    PRIMARY_SCHOOL(3, "学生健康状况及影响因素调查表（小学版）"),
    MIDDLE_SCHOOL(4, "学生健康状况及影响因素调查表（中学版）"),
    UNIVERSITY_SCHOOL(5, "学生健康状况及影响因素调查表（大学版）"),
    VISION_SPINE(6, "学生视力不良及脊柱弯曲异常影响因素专项调查表"),
    SCHOOL_ENVIRONMENT(7, "学校环境健康影响因素调查表"),
    ;

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

    public static QuestionnaireTypeEnum getQuestionnaireType(Integer type){
        return Arrays.stream(values())
                .filter(item -> Objects.equals(item.getType(),type))
                .findFirst()
                .orElse(null);
    }
}

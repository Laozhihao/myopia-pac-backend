package com.wupol.myopia.business.common.utils.constant;

import com.wupol.myopia.base.exception.BusinessException;
import lombok.Getter;

import java.util.Arrays;

/**
 * 问卷主标题类型Enum
 *
 * @author Simple4H
 */
@Getter
public enum QuestionnaireMainTitleEnum {

    NOTICE(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType(), CommonConst.QUESTIONNAIRE_MAIN_TITLE_NOTICE),
    VISION(QuestionnaireTypeEnum.VISION_SPINE.getType(), CommonConst.QUESTIONNAIRE_MAIN_TITLE_VISION),
    PRIMARY_STUDENT(QuestionnaireTypeEnum.PRIMARY_SCHOOL.getType(), CommonConst.QUESTIONNAIRE_MAIN_TITLE_HEALTHY),
    MIDDLE_STUDENT(QuestionnaireTypeEnum.MIDDLE_SCHOOL.getType(), CommonConst.QUESTIONNAIRE_MAIN_TITLE_HEALTHY),
    UNIVERSITY_STUDENT(QuestionnaireTypeEnum.UNIVERSITY_SCHOOL.getType(), CommonConst.QUESTIONNAIRE_MAIN_TITLE_HEALTHY),
    A_1(QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL.getType(), QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL.getDesc()),
    A_2(QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getType(), QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getDesc()),
    A_3(QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getType(), QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getDesc());


    /**
     * 类型
     **/

    private final Integer type;

    /**
     * 主标题
     **/
    private final String mainTitle;

    QuestionnaireMainTitleEnum(Integer type, String mainTitle) {
        this.type = type;
        this.mainTitle = mainTitle;
    }

    public static QuestionnaireMainTitleEnum getByType(Integer type) {
        return Arrays.stream(QuestionnaireMainTitleEnum.values())
                .filter(item -> item.type.equals(type))
                .findFirst()
                .orElseThrow(() -> new BusinessException("无效类型"));
    }

}

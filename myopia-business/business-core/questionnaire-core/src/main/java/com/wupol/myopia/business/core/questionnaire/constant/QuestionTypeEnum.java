package com.wupol.myopia.business.core.questionnaire.constant;

import lombok.Getter;

/**
 * 问题类型枚举
 *
 * @author Simple4H
 */
@Getter
public enum QuestionTypeEnum {

    RADIO("radio", "单选"),
    CHECKBOX("checkbox", "多选"),
    TITLE("title", "标题");

    /**
     * 类型
     **/
    private final String type;
    /**
     * 名称
     **/
    private final String desc;

    QuestionTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}

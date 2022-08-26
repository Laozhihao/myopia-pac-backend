package com.wupol.myopia.business.core.questionnaire.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 问题类型枚举
 *
 * @author Simple4H
 */
@Getter
public enum QuestionTypeEnum {

    INPUT("input", "输入框"),
    RADIO("radio", "单选"),
    RADIO_INPUT("radio-input", "单选"),
    CHECKBOX("checkbox", "多选"),
    CHECKBOX_INPUT("checkbox-input", "多选"),
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

    public static QuestionTypeEnum getType(String type){
         return Arrays.stream(values())
                .filter(item -> Objects.equals(type,item.getType()))
                .findFirst().orElse(null);
    }
}

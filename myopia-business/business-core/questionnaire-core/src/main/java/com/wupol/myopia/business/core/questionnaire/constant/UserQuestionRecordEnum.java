package com.wupol.myopia.business.core.questionnaire.constant;

import lombok.Getter;

/**
 * 进度枚举
 *
 * @author Simple4H
 */
@Getter
public enum UserQuestionRecordEnum {

    NOT_START(0, "未开始"),
    PROCESSING(1, "进行中"),
    FINISH(2, "结束");

    /**
     * 类型
     **/
    private final Integer type;
    /**
     * 名称
     **/
    private final String desc;

    UserQuestionRecordEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}

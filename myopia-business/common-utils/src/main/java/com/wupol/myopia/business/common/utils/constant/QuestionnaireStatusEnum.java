package com.wupol.myopia.business.common.utils.constant;

import lombok.Getter;

/**
 * 问卷状态
 *
 * @author xz
 */
@Getter
public enum QuestionnaireStatusEnum {

    NOT_START(0, "未开始"),
    IN_PROGRESS(1, "进行中"),
    FINISH(2, "已完成");

    /**
     * 状态
     **/

    private final Integer code;
    /**
     * 描述
     **/
    private final String desc;

    QuestionnaireStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

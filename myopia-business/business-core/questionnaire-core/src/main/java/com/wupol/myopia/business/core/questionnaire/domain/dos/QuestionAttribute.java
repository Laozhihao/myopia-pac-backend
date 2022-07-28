package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 问卷-属性
 *
 * @author Simple4H
 */
@Getter
@Setter
public class QuestionAttribute implements Serializable {

    private static final long serialVersionUID = -3508337043252065027L;
    /**
     * 是否必填
     */
    private Boolean required;

    /**
     * 是否统计
     */
    private Boolean statistics;

    /**
     * 是否标题
     */
    private Boolean onlyTitle;

    /**
     * 是否隐藏
     */
    private Boolean isHidden;

    /**
     * 是否记分题目
     */
    private Boolean isScore;
}

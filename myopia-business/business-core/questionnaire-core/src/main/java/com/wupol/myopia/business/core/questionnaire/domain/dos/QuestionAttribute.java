package com.wupol.myopia.business.core.questionnaire.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * 问卷-属性
 *
 * @author Simple4H
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class QuestionAttribute {

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

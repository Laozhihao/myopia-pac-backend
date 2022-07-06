package com.wupol.myopia.business.core.questionnaire.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 问卷-属性
 *
 * @author Simple4H
 */
@Getter
@Setter
public class Attribute {

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
    private Boolean isTitle;
}

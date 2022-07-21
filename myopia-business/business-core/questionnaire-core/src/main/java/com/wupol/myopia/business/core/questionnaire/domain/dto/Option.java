package com.wupol.myopia.business.core.questionnaire.domain.dto;

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
public class Option {

    /**
     * id
     */
    private String id;

    /**
     * 标题
     */
    private String text;

    /**
     * 属性
     */
    private OptionAttribute attribute;

    /**
     * 系统序号
     */
    private String serialNumber;
}

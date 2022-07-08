package com.wupol.myopia.business.core.questionnaire.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 选项答案
 *
 * @author Simple4H
 */
@Getter
@Setter
public class OptionAnswer {

    /**
     * 选项Id
     */
    private Integer optionId;

    /**
     * 文本值
     */
    private String text;

    /**
     * 占位符值
     */
    private String value;
}

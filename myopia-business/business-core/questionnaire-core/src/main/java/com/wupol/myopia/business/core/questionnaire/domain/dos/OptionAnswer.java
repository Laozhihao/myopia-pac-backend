package com.wupol.myopia.business.core.questionnaire.domain.dos;

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
    private String optionId;

    /**
     * 文本
     */
    private String text;

    /**
     * 填空值
     */
    private String value;
}

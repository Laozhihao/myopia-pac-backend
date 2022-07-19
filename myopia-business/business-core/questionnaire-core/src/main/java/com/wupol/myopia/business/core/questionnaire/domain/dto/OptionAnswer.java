package com.wupol.myopia.business.core.questionnaire.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
     * 文本值
     */
    private String text;

    /**
     * 填空值
     */
    private String value;

    /**
     * 填空题
     */
    private List<OptionAnswer> children;
}

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
     * 填空题
     */
    private List<AnswerInput> children;

    @Getter
    @Setter
    public static class AnswerInput {

        /**
         * 填空题
         */
        private String answerInputId;

        /**
         * 填空值
         */
        private String value;
    }
}

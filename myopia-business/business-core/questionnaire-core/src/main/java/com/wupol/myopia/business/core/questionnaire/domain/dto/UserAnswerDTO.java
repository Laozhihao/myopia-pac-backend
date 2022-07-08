package com.wupol.myopia.business.core.questionnaire.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用户答案保存
 *
 * @author Simple4H
 */
@Getter
@Setter
public class UserAnswerDTO {

    /**
     * 问卷Id
     */
    private Integer questionnaireId;

    /**
     * 题目
     */
    private List<QuestionDTO> questionList;

    @Getter
    @Setter
    public static class QuestionDTO {

        /**
         * 问题Id
         */
        private Integer questionId;

        /**
         * 答案
         */
        private List<OptionAnswer> answer;
    }
}

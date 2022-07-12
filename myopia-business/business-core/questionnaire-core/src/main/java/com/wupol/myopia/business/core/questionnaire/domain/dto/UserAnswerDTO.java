package com.wupol.myopia.business.core.questionnaire.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
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
    @NotNull(message = "问卷Id不能为空")
    private Integer questionnaireId;

    /**
     * 是否完成 true-提交 false-保存
     */
    @NotNull(message = "isFinish不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Boolean isFinish;

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
         * 文本
         */
        private String text;

        /**
         * 答案
         */
        private List<OptionAnswer> answer;
    }
}

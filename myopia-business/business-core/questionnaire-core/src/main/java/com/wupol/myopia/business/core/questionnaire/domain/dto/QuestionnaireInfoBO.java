package com.wupol.myopia.business.core.questionnaire.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 问卷信息
 *
 * @author hang.yuan 2022/7/20 17:03
 */
@Data
public class QuestionnaireInfoBO {
    /**
     * 问卷Id
     */
    private Integer questionnaireId;

    /**
     * 问卷名称
     */
    private String questionnaireName;

    /**
     * 题目
     */
    private List<QuestionBO> questionList;

    @Data
    public static class QuestionBO{
        /**
         * 问题Id
         */
        private Integer questionId;
        /**
         * 问题名称
         */
        private String questionName;
        /**
         * 问题的序号
         */
        private String questionSerialNumber;

        /**
         * 子问题
         */
        private List<QuestionBO> questionBOList;

    }

}

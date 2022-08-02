package com.wupol.myopia.business.core.questionnaire.domain.dos;

import cn.hutool.core.util.StrUtil;
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
         * 问卷问题关联表ID
         */
        private Integer questionnaireQuestionId;
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
         * 是否记分题
         */
        private Boolean isScore;

        /**
         * 子问题
         */
        private List<QuestionBO> questionBOList;




        public String getQuestionSerialNumber() {
            if (StrUtil.isBlank(questionSerialNumber)){
                return StrUtil.EMPTY;
            }
            return questionSerialNumber;
        }
    }

}

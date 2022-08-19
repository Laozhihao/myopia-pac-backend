package com.wupol.myopia.business.core.questionnaire.domain.dos;

import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 问卷信息
 *
 * @author hang.yuan 2022/7/20 17:03
 */
@Data
public class QuestionnaireInfoBO {

    /**
     * 问卷对象
     */
    private Questionnaire questionnaire;

    /**
     * 题目
     */
    private List<QuestionBO> questionList;


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class QuestionBO extends Question {

        /**
         * 问卷问题关联表ID
         */
        private Integer questionnaireQuestionId;

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

        /**
         * qes信息
         */
        private List<QesDataDO> qesData;



        public String getQuestionSerialNumber() {
            if (StrUtil.isBlank(questionSerialNumber)){
                return StrUtil.EMPTY;
            }
            return questionSerialNumber;
        }
    }

}

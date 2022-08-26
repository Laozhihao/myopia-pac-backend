package com.wupol.myopia.business.core.questionnaire.domain.dos;

import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import lombok.Data;

import java.util.List;

/**
 * 问卷问题树实体
 *
 * @author hang.yuan 2022/8/8 16:10
 */
@Data
public class QuestionnaireAndQuestionBO {
    /**
     * 问卷
     */
    private Questionnaire questionnaire;
    /**
     * 问卷包含的问题集合
     */
    private List<QuestionExtBO> questionList;
}

package com.wupol.myopia.business.core.questionnaire.domain.dos;

import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 问题扩展实体
 *
 * @author hang.yuan 2022/8/8 16:19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionExtBO extends Question {
    /**
     * 问卷问题关联表ID
     */
    private Integer questionnaireQuestionId;

    private List<QuestionExtBO> questionExtBOList;
}

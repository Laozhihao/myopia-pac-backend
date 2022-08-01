package com.wupol.myopia.business.core.questionnaire.domain.dto;

import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 *  逻辑题目返回
 *
 * @author Simple4H
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
public class LogicFindQuestionResponseDTO extends QuestionnaireQuestion {

    /**
     * 标题
     */
    private String title;
}

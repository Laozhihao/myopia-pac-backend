package com.wupol.myopia.business.core.questionnaire.domain.dto;

import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 问卷返回
 *
 * @author Simple4H
 */
@Getter
@Setter
public class QuestionnaireResponseDTO extends Question {

    @Getter
    private List<Question> questionList;


}

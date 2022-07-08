package com.wupol.myopia.business.core.questionnaire.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QuestionnaireQuestionMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Service
public class QuestionnaireQuestionService extends BaseService<QuestionnaireQuestionMapper, QuestionnaireQuestion> {

    public List<QuestionnaireQuestion> getByQuestionnaireId(Integer questionnaireId) {
        return baseMapper.getByQuestionnaireId(questionnaireId);
    }


}

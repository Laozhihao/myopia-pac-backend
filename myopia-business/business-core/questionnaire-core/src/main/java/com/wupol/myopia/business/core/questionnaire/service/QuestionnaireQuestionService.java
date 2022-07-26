package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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


    /**
     * 通过问卷Id获取QuestionnaireQuestion
     * @param questionnaireId
     * @return
     */
    public List<QuestionnaireQuestion> listByQuestionnaireId(Integer questionnaireId) {
        return this.list(new LambdaQueryWrapper<QuestionnaireQuestion>()
                .eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId)
                .orderByAsc(QuestionnaireQuestion::getId));
    }
}

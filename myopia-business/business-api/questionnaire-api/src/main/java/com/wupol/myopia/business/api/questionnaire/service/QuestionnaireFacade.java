package com.wupol.myopia.business.api.questionnaire.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 问卷
 *
 * @author hang.yuan 2022/7/20 16:40
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class QuestionnaireFacade {

    private final QuestionnaireService questionnaireService;
    private final QuestionnaireQuestionService questionnaireQuestionService;
    private final QuestionService questionService;

    public void getQuestionnaireInfo(){
        Integer questionnaireId = 2;
        List<QuestionnaireQuestion> questionnaireQuestionList = questionnaireQuestionService.listByQuestionnaireId(questionnaireId);
        if (CollectionUtil.isNotEmpty(questionnaireQuestionList)){
            List<Integer> questionIds = questionnaireQuestionList.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
            List<Question> questionList = questionService.listByIds(questionIds);
        }
    }
}

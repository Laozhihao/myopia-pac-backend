package com.wupol.myopia.business.core.questionnaire.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionnaireResponseDTO;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QuestionnaireMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Service
public class QuestionnaireService extends BaseService<QuestionnaireMapper, Questionnaire> {

    @Resource
    private QuestionnaireQuestionService questionnaireQuestionService;

    @Resource
    private QuestionService questionService;

    public QuestionnaireResponseDTO getQuestionnaireResponseById(Integer id) {
        QuestionnaireResponseDTO response = baseMapper.getQuestionnaireResponseById(id);
        List<Integer> collect = questionnaireQuestionService.getByQuestionnaireId(id)
                .stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
        response.setQuestionList(questionService.getByIds(collect));
        return response;
    }

}

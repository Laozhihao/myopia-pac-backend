package com.wupol.myopia.business.api.management.service;

import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.core.questionnaire.domain.dto.LogicDeletedRequestDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.LogicEditRequestDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.LogicFindQuestionResponseDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionResponse;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQuestionService;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 题目中间表管理
 *
 * @author Simple4H
 */
@Service
public class QuestionnaireQuestionBizService {

    @Resource
    private QuestionnaireQuestionService questionnaireQuestionService;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionnaireService questionnaireService;

    public List<QuestionResponse> logicList(Integer questionnaireId) {
        List<QuestionResponse> responses = new ArrayList<>();
        List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionService.logicList(questionnaireId);
        if (CollectionUtils.isEmpty(questionnaireQuestions)) {
            return responses;
        }
        Map<Integer, Question> questionMaps = questionService.getByIds(questionnaireQuestions.stream()
                        .map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        questionnaireQuestions.forEach(questionnaireQuestion ->
                responses.add(questionnaireService.commonBuildQuestion(questionMaps.get(questionnaireQuestion.getQuestionId()), questionnaireQuestion)));
        return responses;
    }

    /**
     * 设置逻辑题
     */
    @Transactional(rollbackFor = Exception.class)
    public void editLogic(LogicEditRequestDTO requestDTO) {

        if (Objects.equals(requestDTO.getIsLogic(), Boolean.FALSE)) {
            return;
        }
        QuestionnaireQuestion questionnaireQuestion = questionnaireQuestionService.getByQuestionnaireIdAndQuestionId(requestDTO.getQuestionnaireId(), requestDTO.getQuestionId());
        if (Objects.isNull(questionnaireQuestion)) {
            return;
        }
        questionnaireQuestion.setIsLogic(Boolean.TRUE);
        questionnaireQuestion.setJumpIds(requestDTO.getJumpIds());
        questionnaireQuestionService.updateById(questionnaireQuestion);
    }

    @Transactional(rollbackFor = Exception.class)
    public void editDeleted(LogicDeletedRequestDTO requestDTO) {
        Integer questionnaireId = requestDTO.getQuestionnaireId();
        Integer questionId = requestDTO.getQuestionId();

        QuestionnaireQuestion questionnaireQuestion = questionnaireQuestionService.getByQuestionnaireIdAndQuestionId(questionnaireId, questionId);
        if (Objects.isNull(questionnaireQuestion)) {
            throw new BusinessException("问题异常！");
        }
        questionnaireQuestionService.deletedLogic(questionnaireId, questionId);
    }

    public List<LogicFindQuestionResponseDTO> logicFindQuestion(Integer questionnaireId, String serialNumber, Integer questionId) {
        List<LogicFindQuestionResponseDTO> response = new ArrayList<>();
        List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionService.getByQuestionnaireIdSerialNumber(questionnaireId, serialNumber, questionId);

        if (CollectionUtils.isEmpty(questionnaireQuestions)) {
            return response;
        }

        Set<Integer> questionIds = questionnaireQuestions.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toSet());
        Map<Integer, String> questionTitleMap = questionService.getByIds(questionIds).stream().collect(Collectors.toMap(Question::getId, Question::getTitle));
        questionnaireQuestions.forEach(questionnaireQuestion -> {
            LogicFindQuestionResponseDTO responseDTO = new LogicFindQuestionResponseDTO();
            BeanUtils.copyProperties(questionnaireQuestion, responseDTO);
            responseDTO.setTitle(questionTitleMap.get(questionnaireQuestion.getQuestionId()));
            response.add(responseDTO);
        });
        return response;
    }


}

package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.core.questionnaire.domain.dto.LogicDeletedRequestDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.LogicEditRequestDTO;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import com.wupol.myopia.business.core.questionnaire.service.QuestionnaireQuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 题目中间表管理
 *
 * @author Simple4H
 */
@Service
public class QuestionnaireQuestionBizService {

    @Resource
    private QuestionnaireQuestionService questionnaireQuestionService;

    /**
     * 设置逻辑题
     */
    @Transactional(rollbackFor = Exception.class)
    public void editLogic(LogicEditRequestDTO requestDTO) {
        QuestionnaireQuestion questionnaireQuestion = questionnaireQuestionService.getByQuestionnaireIdAndQuestionId(requestDTO.getQuestionnaireId(), requestDTO.getQuestionId());
        if (Objects.isNull(questionnaireQuestion)) {
            return;
        }
        questionnaireQuestion.setIsLogic(Boolean.TRUE);
        questionnaireQuestion.setJumpIds(requestDTO.getJumpIds());
        questionnaireQuestionService.updateById(questionnaireQuestion);
    }

    @Transactional(rollbackFor = Exception.class)
    public void editDeleted(LogicDeletedRequestDTO requestDTO ) {
        QuestionnaireQuestion questionnaireQuestion = questionnaireQuestionService.getByQuestionnaireIdAndQuestionId(requestDTO.getQuestionnaireId(), requestDTO.getQuestionId());
        if (Objects.isNull(questionnaireQuestion)) {
            throw new BusinessException("问题异常！");
        }
        questionnaireQuestion.setIsLogic(null);
        questionnaireQuestion.setJumpIds(null);
        questionnaireQuestionService.updateById(questionnaireQuestion);
    }


}

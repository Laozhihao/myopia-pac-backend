package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.dto.EditQuestionnaireRequestDTO;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QuestionnaireQuestionMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

import java.util.List;

/**
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Service
public class QuestionnaireQuestionService extends BaseService<QuestionnaireQuestionMapper, QuestionnaireQuestion> {

    /**
     * 通过questionnaireId获取
     *
     * @param questionnaireId 问卷Id
     *
     * @return List<QuestionnaireQuestion>
     */
    public List<QuestionnaireQuestion> getByQuestionnaireId(Integer questionnaireId) {
        LambdaQueryWrapper<QuestionnaireQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId);
        return baseMapper.selectList(wrapper);
    }

    public void deletedByQuestionnaireId(Integer questionnaireId) {
        LambdaQueryWrapper<QuestionnaireQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId);
        baseMapper.delete(wrapper);
    }

    public void insert(Integer questionnaireId, List<EditQuestionnaireRequestDTO.Detail> details, Integer pid) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        details.forEach(detail -> {
            QuestionnaireQuestion question = new QuestionnaireQuestion();
            question.setQuestionnaireId(questionnaireId);
            question.setQuestionId(detail.getId());
            question.setPid(pid);
            question.setSerialNumber(detail.getSerialNumber());
            question.setJumpIds(detail.getJumpIds());
            question.setRequired(detail.getRequired());
            question.setSort(1);
            baseMapper.insert(question);
            List<EditQuestionnaireRequestDTO.Detail> questionList = detail.getQuestionList();
            if (!CollectionUtils.isEmpty(questionList)) {
                insert(questionnaireId, questionList, question.getId());
            }
        });
    }

    public List<QuestionnaireQuestion> getBySerialNumbers(Integer questionnaireId, Collection<String> serialNumber) {
        LambdaQueryWrapper<QuestionnaireQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId);
        wrapper.in(QuestionnaireQuestion::getSerialNumber, serialNumber);
        return baseMapper.selectList(wrapper);
    }

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

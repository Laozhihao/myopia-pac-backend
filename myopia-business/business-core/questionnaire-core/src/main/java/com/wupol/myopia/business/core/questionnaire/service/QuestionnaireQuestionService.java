package com.wupol.myopia.business.core.questionnaire.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.dto.EditQuestionnaireRequestDTO;
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
        if (CollectionUtil.isEmpty(details)) {
            return;
        }
        details.forEach(detail -> {
            QuestionnaireQuestion question = new QuestionnaireQuestion();
            question.setQuestionnaireId(questionnaireId);
            question.setQuestionId(detail.getPartId());
            question.setPid(pid);
            question.setSerialNumber(detail.getSerialNumber());
            question.setJumpIds(detail.getJumpIds());
            question.setRequired(detail.getRequired());
            question.setSort(1);
            baseMapper.insert(question);
            List<EditQuestionnaireRequestDTO.Detail2> children = detail.getQuestionList();
            if (CollectionUtil.isNotEmpty(children)) {
                insert2(questionnaireId, children, question.getId());
            }
        });
    }

    private void insert2(Integer questionnaireId, List<EditQuestionnaireRequestDTO.Detail2> details, Integer pid) {
        if (CollectionUtil.isEmpty(details)) {
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
            List<EditQuestionnaireRequestDTO.Detail2> children = detail.getQuestionList();
            if (CollectionUtil.isNotEmpty(children)) {
                insert2(questionnaireId, children, question.getId());
            }
        });
    }


}

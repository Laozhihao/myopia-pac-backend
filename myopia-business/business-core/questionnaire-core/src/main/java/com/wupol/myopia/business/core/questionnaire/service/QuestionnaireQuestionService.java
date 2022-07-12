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


}

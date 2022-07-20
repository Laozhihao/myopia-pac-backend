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
     * 根据问卷ID获取问卷和问题关系
     *
     * @param questionnaireId 问卷ID
     */
    public List<QuestionnaireQuestion> listByQuestionnaireId(Integer questionnaireId){
        LambdaQueryWrapper<QuestionnaireQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionnaireQuestion::getQuestionnaireId,questionnaireId);
        return baseMapper.selectList(queryWrapper);
    }

}

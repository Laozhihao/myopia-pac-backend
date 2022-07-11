package com.wupol.myopia.business.core.questionnaire.service;

import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QuestionMapper;
import com.wupol.myopia.base.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Service
public class QuestionService extends BaseService<QuestionMapper, Question> {

    public Question getByQuestionId(Integer id) {
        return baseMapper.getByQuestionId(id);
    }

    public List<Question> getByIds(List<Integer> ids) {
        return baseMapper.getByIds(ids);
    }

}

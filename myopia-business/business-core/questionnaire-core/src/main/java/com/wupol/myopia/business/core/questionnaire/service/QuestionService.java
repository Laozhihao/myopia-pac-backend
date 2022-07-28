package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QuestionMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Service
public class QuestionService extends BaseService<QuestionMapper, Question> {

    /**
     * 获取问题
     *
     * @param name    标题
     * @param isTitle 是否标题
     *
     * @return List<Question>
     */
    public List<Question> searchQuestion(String name, Boolean isTitle) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Question::getTitle, name)
                .eq(Objects.equals(isTitle, Boolean.TRUE), Question::getType, "title");
        return baseMapper.selectList(wrapper);
    }

    /**
     * 获取所有问题
     *
     * @return List<Question>
     */
    public List<Question> getAllQuestion() {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(Question::getType, "title");
        return baseMapper.selectList(queryWrapper);
    }
}

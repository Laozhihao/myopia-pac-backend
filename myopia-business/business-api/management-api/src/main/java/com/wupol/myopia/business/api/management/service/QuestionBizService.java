package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 题库管理
 *
 * @author Simple4H
 */
@Service
public class QuestionBizService {

    @Resource
    private QuestionService questionService;

    @Transactional(rollbackFor = Exception.class)
    public void saveQuestion(Question question) {
        //TODO: 选项Id唯一判断
        questionService.save(question);
    }
}

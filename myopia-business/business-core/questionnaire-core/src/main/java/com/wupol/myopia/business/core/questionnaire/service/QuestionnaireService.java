package com.wupol.myopia.business.core.questionnaire.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionResponse;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionnaireInfoDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionnaireResponseDTO;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QuestionnaireMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

    /**
     * 获取问卷问题
     *
     * @param questionnaireId
     */
    public List<QuestionnaireInfoDTO> getQuestionnaire(Integer questionnaireId) {
        Questionnaire questionnaire = this.getById(questionnaireId);
        Assert.notNull(questionnaire, "问卷不存在！");
        if (StrUtil.isNotBlank(questionnaire.getPageJson())) {
            return JSONObject.parseArray(questionnaire.getPageJson(), QuestionnaireInfoDTO.class);
        }
        //如果没有页面数据，组装问卷数据
        List<QuestionnaireInfoDTO> questionnaireInfo = getQuestionnaireInfo(questionnaireId);
        //todo 将数据存储进pageJson字段
        //  this.updateById(Questionnaire.builder().pageJson(JSONObject.toJSONString(questionnaireInfo)).id(questionnaireId).build());
        return questionnaireInfo;
    }


    /**
     * 组装问卷问题数据
     *
     * @return
     */
    protected List<QuestionnaireInfoDTO> getQuestionnaireInfo(Integer questionnaireId) {
        ArrayList<QuestionnaireInfoDTO> infoDTOS = Lists.newArrayList();
        List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionService.list(new LambdaQueryWrapper<QuestionnaireQuestion>().eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId));
        if (CollectionUtil.isNotEmpty(questionnaireQuestions)) {
            List<Integer> questionIds = questionnaireQuestions.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
            List<Question> questions = questionService.listByIds(questionIds);
            Map<Integer, Question> questionMap = questions.stream().collect(Collectors.toMap(Question::getId, Function.identity()));
            //过滤出顶层区域
            List<QuestionnaireQuestion> partLists = questionnaireQuestions.stream().filter(it -> QuestionnaireQuestion.TOP_PARENT_ID == it.getPid().intValue()).sorted(Comparator.comparing(e -> e.getSort())).collect(Collectors.toList());
            partLists.stream().forEach(it -> {
                Question question = questionMap.get(it.getQuestionId());
                QuestionnaireInfoDTO questionnaireInfoDTO = QuestionnaireInfoDTO.builder().partId(it.getId()).partName(question.getTitle()).partId(question.getId()).build();
                List<QuestionResponse> questionList = Lists.newArrayList();
                List<QuestionnaireQuestion> childQuestion = questionnaireQuestions.stream().filter(cache -> it.getId().equals(cache.getPid())).sorted(Comparator.comparing(e -> e.getSort())).collect(Collectors.toList());
                //构建此模块下的所有问题
                childQuestion.stream().forEach(child -> {
                    Question createQuestion = questionMap.get(child.getQuestionId());
                    if (Question.TOP_PARENT_ID == createQuestion.getPid().intValue()) {
                        QuestionResponse questionResponse = BeanCopyUtil.copyBeanPropertise(createQuestion, QuestionResponse.class);
                        setChildren(questionResponse, childQuestion, questionMap);
                        questionList.add(questionResponse);
                    }
                });
                questionnaireInfoDTO.setQuestionList(questionList);
                infoDTOS.add(questionnaireInfoDTO);
            });
        }
        return infoDTOS;
    }


    /**
     * 递归封装子问题
     * @param questionResponse
     * @param childQuestion
     * @param questionMap
     */
    protected void setChildren(QuestionResponse questionResponse, List<QuestionnaireQuestion> childQuestion, Map<Integer, Question> questionMap) {
        ArrayList<QuestionResponse> children = Lists.newArrayList();
        List<Question> questions = childQuestion.stream().map(it -> {
            Question question = questionMap.get(it.getQuestionId());
            return question;
        }).collect(Collectors.toList());

        questions = questions.stream().filter(it -> it.getPid().intValue() == questionResponse.getId().intValue()).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(questions)) {
            questions.forEach(cache -> {
                QuestionResponse child = BeanCopyUtil.copyBeanPropertise(cache, QuestionResponse.class);
                setChildren(child, childQuestion, questionMap);
                children.add(child);
            });
            questionResponse.setChildren(children);
        }
    }
}

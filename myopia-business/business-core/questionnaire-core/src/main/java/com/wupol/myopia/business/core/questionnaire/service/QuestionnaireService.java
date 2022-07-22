package com.wupol.myopia.business.core.questionnaire.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.business.core.questionnaire.domain.dto.JumpIdsDO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.Option;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionResponse;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionnaireInfoDTO;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QuestionnaireMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
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
        this.updateById(Questionnaire.builder().pageJson(JSONObject.toJSONString(questionnaireInfo)).id(questionnaireId).build());
        return questionnaireInfo;
    }


    /**
     * 组装问卷问题数据
     *
     * @return
     */
    protected List<QuestionnaireInfoDTO> getQuestionnaireInfo(Integer questionnaireId) {
        ArrayList<QuestionnaireInfoDTO> infoDTOS = Lists.newArrayList();
        List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionService.listByQuestionnaireId(questionnaireId);
        if (CollectionUtil.isNotEmpty(questionnaireQuestions)) {
            List<Integer> questionIds = questionnaireQuestions.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
            List<Question> questions = questionService.listByIds(questionIds);
            Map<Integer, Question> questionMap = questions.stream().collect(Collectors.toMap(Question::getId, Function.identity()));
            //过滤出顶层区域
            List<QuestionnaireQuestion> partLists = questionnaireQuestions.stream()
                    .filter(it -> QuestionnaireQuestion.TOP_PARENT_ID == it.getPid())
                    .sorted(Comparator.comparing(QuestionnaireQuestion::getId)).collect(Collectors.toList());
            partLists.forEach(it -> {
                Question question = questionMap.get(it.getQuestionId());
                QuestionnaireInfoDTO questionnaireInfoDTO = BeanCopyUtil.copyBeanPropertise(question, QuestionnaireInfoDTO.class);
                List<QuestionResponse> questionList = Lists.newArrayList();
                //构建此模块下的所有问题
                questionnaireQuestions.forEach(child -> {
                    if (it.getId().equals(child.getPid())) {
                        Question createQuestion = questionMap.get(child.getQuestionId());
                        QuestionResponse questionResponse = BeanCopyUtil.copyBeanPropertise(createQuestion, QuestionResponse.class);
                        questionResponse.setRequired(child.getRequired());
                        questionResponse.setSerialNumber(child.getSerialNumber());
                        setJumpIds(questionResponse, child.getJumpIds());
                        buildQuestion(questionResponse, child.getId(), questionnaireQuestions, questionMap);
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
     * 设置问题层级关系
     *
     * @param pid
     * @param childQuestion
     * @param questionMap
     */
    protected void buildQuestion(QuestionResponse questionResponse, Integer pid, List<QuestionnaireQuestion> childQuestion, Map<Integer, Question> questionMap) {
        childQuestion.forEach(it -> {
            if (pid.equals(it.getPid())) {
                JumpIdsDO jumpIds = it.getJumpIds();
                Question createQuestion = questionMap.get(it.getQuestionId());
                QuestionResponse childQuestionResponse = BeanCopyUtil.copyBeanPropertise(createQuestion, QuestionResponse.class);
                childQuestionResponse.setRequired(it.getRequired());
                childQuestionResponse.setSerialNumber(it.getSerialNumber());
                setJumpIds(childQuestionResponse, jumpIds);
                List<QuestionResponse> questionResponses = CollectionUtil.isNotEmpty(questionResponse.getQuestionList()) ? questionResponse.getQuestionList() : new ArrayList<>();
                questionResponses.add(childQuestionResponse);
                questionResponse.setQuestionList(questionResponses);
                buildQuestion(childQuestionResponse,it.getId(),childQuestion,questionMap);
            }
        });
    }

    /**
     * 封装跳转Id
     *
     * @param questionResponse
     * @param jumpIdsDO
     */
    protected void setJumpIds(QuestionResponse questionResponse, JumpIdsDO jumpIdsDO) {
        List<Option> options = questionResponse.getOptions();
        if (CollectionUtil.isNotEmpty(options) && Objects.nonNull(jumpIdsDO)) {
            options = JSONObject.parseArray(JSONObject.toJSONString(options), Option.class);
            options.forEach(option -> {
                if (option.getId().equals(jumpIdsDO.getOptionId())) {
                    option.setJumpIds(jumpIdsDO.getJumpIds());
                    return;
                }
            });
            questionResponse.setOptions(options);
        }
    }
}

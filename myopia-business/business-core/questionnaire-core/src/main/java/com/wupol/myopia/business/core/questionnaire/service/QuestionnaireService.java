package com.wupol.myopia.business.core.questionnaire.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.core.questionnaire.domain.dto.*;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QuestionnaireMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
     * 获取问卷列表
     *
     * @param year 年份
     *
     * @return List<Questionnaire>
     */
    public List<Questionnaire> getQuestionnaireList(Integer year) {
        // 默认今年
        if (Objects.isNull(year)) {
            year = DateUtil.getYear(new Date());
        }
        return getByYear(year);
    }

    /**
     * 通过年份获取
     *
     * @param year 年份
     *
     * @return List<Questionnaire>
     */
    public List<Questionnaire> getByYear(Integer year) {
        LambdaQueryWrapper<Questionnaire> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Questionnaire::getYear, year);
        return baseMapper.selectList(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void editQuestionnaire(EditQuestionnaireRequestDTO requestDTO) {
        Integer questionnaireId = requestDTO.getQuestionnaireId();
        questionnaireQuestionService.deletedByQuestionnaireId(questionnaireId);
        questionnaireQuestionService.insert(questionnaireId, requestDTO.getDetail(), -1);

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
            List<QuestionnaireQuestion> partLists = questionnaireQuestions.stream().filter(it -> QuestionnaireQuestion.TOP_PARENT_ID == it.getPid()).sorted(Comparator.comparing(QuestionnaireQuestion::getSort)).collect(Collectors.toList());
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
                List<QuestionResponse> questionResponses = CollectionUtil.isNotEmpty(questionResponse.getChildren()) ? questionResponse.getChildren() : new ArrayList<>();
                questionResponses.add(childQuestionResponse);
                questionResponse.setChildren(questionResponses);
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

    /**
     * 获取问卷信息
     *
     * @param id 问卷id
     *
     * @return QuestionnaireResponseDTO
     */
    public QuestionnaireResponseDTO getDetailByQuestionnaireId(Integer id) {
        Questionnaire questionnaire = this.getById(id);
        QuestionnaireResponseDTO responseDTO = new QuestionnaireResponseDTO();
        if (Objects.isNull(questionnaire)) {
            return responseDTO;
        }
        responseDTO.setId(questionnaire.getId());
        responseDTO.setTitle(questionnaire.getTitle());
        responseDTO.setYear(questionnaire.getYear());
        responseDTO.setDetail(getQuestionnaireInfo(id));
        return responseDTO;
    }

    /**
     * 通过年份、类型获取
     *
     * @param year  年份
     * @param types 类型
     *
     * @return List<Questionnaire>
     */
    public List<Questionnaire> getByYearAndTypes(Collection<Integer> types, Integer year) {
        LambdaQueryWrapper<Questionnaire> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Questionnaire::getYear, year).in(Questionnaire::getType, types);
        return baseMapper.selectList(wrapper);
    }
}

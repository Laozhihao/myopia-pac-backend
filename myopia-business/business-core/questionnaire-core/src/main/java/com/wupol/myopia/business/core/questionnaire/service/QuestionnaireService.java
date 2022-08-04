package com.wupol.myopia.business.core.questionnaire.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.BeanCopyUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.dos.*;
import com.wupol.myopia.business.core.questionnaire.domain.dto.EditQuestionnaireRequestDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionResponse;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionnaireInfoDTO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionnaireResponseDTO;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QuestionnaireMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import com.wupol.myopia.business.core.questionnaire.util.DropSelectUtil;
import org.apache.commons.lang3.StringUtils;
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
        return findByList(new Questionnaire().setYear(year));
    }

    /**
     * 编辑问卷
     */
    @Transactional(rollbackFor = Exception.class)
    public void editQuestionnaire(EditQuestionnaireRequestDTO requestDTO) {
        Integer questionnaireId = requestDTO.getQuestionnaireId();
        questionnaireQuestionService.remove(new QuestionnaireQuestion().setQuestionnaireId(questionnaireId));
        questionnaireQuestionService.insert(questionnaireId, requestDTO.getDetail(), -1);
        // 更新问卷信息
        updateTime(questionnaireId);
    }

    /**
     * 获取问卷问题
     *
     * @param questionnaireId
     */
    public List<QuestionnaireInfoDTO> getQuestionnaire(Integer questionnaireId) {
        Questionnaire questionnaire = this.getById(questionnaireId);
        Assert.notNull(questionnaire, "问卷不存在！");
//        if (CollectionUtil.isNotEmpty(questionnaire.getPageJson())) {
//            return questionnaire.getPageJson();
//        }
        //如果没有页面数据，组装问卷数据
        List<QuestionnaireInfoDTO> questionnaireInfo = getQuestionnaireInfo(questionnaireId);
//        this.updateById(Questionnaire.builder().pageJson(questionnaireInfo).id(questionnaireId).build());
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
        if (CollUtil.isEmpty(questionnaireQuestions)) {
            return Collections.emptyList();
        }
        List<Integer> questionIds = questionnaireQuestions.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
        List<Question> questions = questionService.listByIds(questionIds);
        Map<Integer, Question> questionMap = questions.stream().collect(Collectors.toMap(Question::getId, Function.identity()));
        //过滤出顶层区域
        List<QuestionnaireQuestion> partLists = questionnaireQuestions.stream()
                .filter(it -> QuestionnaireQuestion.TOP_PARENT_ID == it.getPid())
                .sorted(Comparator.comparing(QuestionnaireQuestion::getSort)).collect(Collectors.toList());
        partLists.forEach(it -> {
            Question question = questionMap.get(it.getQuestionId());
            QuestionnaireInfoDTO questionnaireInfoDTO = BeanCopyUtil.copyBeanPropertise(question, QuestionnaireInfoDTO.class);
            questionnaireInfoDTO.setExId(it.getId());
            questionnaireInfoDTO.setExPid(it.getPid());
            questionnaireInfoDTO.setIsNotShowNumber(it.getIsNotShowNumber());
            questionnaireInfoDTO.setSerialNumber(it.getSerialNumber());
            questionnaireInfoDTO.setIsLogic(it.getIsLogic());
            questionnaireInfoDTO.setJumpIds(it.getJumpIds());
            List<QuestionResponse> questionList = Lists.newArrayList();
            //构建此模块下的所有问题
            questionnaireQuestions.forEach(child -> {
                if (it.getId().equals(child.getPid())) {
                    Question childQuestion = questionMap.get(child.getQuestionId());
                    QuestionResponse questionResponse = commonBuildQuestion(childQuestion, child);
                    buildQuestion(questionResponse, child.getId(), questionnaireQuestions, questionMap);
                    questionList.add(questionResponse);
                }
            });
            questionnaireInfoDTO.setQuestionList(questionList);
            infoDTOS.add(questionnaireInfoDTO);
        });
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
                Question question = questionMap.get(it.getQuestionId());
                QuestionResponse childQuestionResponse = commonBuildQuestion(question, it);
                List<QuestionResponse> questionResponses = CollUtil.isNotEmpty(questionResponse.getQuestionList()) ? questionResponse.getQuestionList() : new ArrayList<>();
                questionResponses.add(childQuestionResponse);
                questionResponse.setQuestionList(questionResponses);
                buildQuestion(childQuestionResponse, it.getId(), childQuestion, questionMap);
            }
        });
    }

    /***
     * 封装问题公共逻辑块
     * @param question
     * @param it
     * @return
     */
    public QuestionResponse commonBuildQuestion(Question question, QuestionnaireQuestion it) {
        QuestionResponse childQuestionResponse = BeanCopyUtil.copyBeanPropertise(question, QuestionResponse.class);
        childQuestionResponse.setRequired(it.getRequired());
        childQuestionResponse.setSerialNumber(it.getSerialNumber());
        childQuestionResponse.setExId(it.getId());
        childQuestionResponse.setExPid(it.getPid());
        childQuestionResponse.setIsNotShowNumber(it.getIsNotShowNumber());
        childQuestionResponse.setIsLogic(it.getIsLogic());
        childQuestionResponse.setJumpIds(it.getJumpIds());
        setJumpIds(childQuestionResponse, it.getJumpIds());
        if (StringUtils.equals(question.getType(), "infectious-disease-table")) {
            setTable(childQuestionResponse, question.getTableJson());
        }
        return childQuestionResponse;
    }

    /**
     * 封装跳转Id
     *
     * @param questionResponse
     * @param jumpIdsDO
     */
    protected void setJumpIds(QuestionResponse questionResponse, List<JumpIdsDO> jumpIdsDO) {
        List<Option> options = questionResponse.getOptions();
        if (CollUtil.isEmpty(options) || Objects.isNull(jumpIdsDO)) {
            return;
        }
        Map<String, JumpIdsDO> jumpIdsDOMap = jumpIdsDO.stream().collect(Collectors.toMap(JumpIdsDO::getOptionId, Function.identity()));
        options.forEach(option -> {
            JumpIdsDO result = jumpIdsDOMap.get(option.getId());
            if (Objects.nonNull(result)) {
                option.setJumpIds(result.getJumpIds());
            }
        });
        questionResponse.setOptions(options);
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
     * @param types 类型
     *
     * @return List<Questionnaire>
     */
    public List<Questionnaire> getByTypes(Collection<Integer> types) {
        return baseMapper.getByTypes(types);
    }

    /**
     * 更新问卷时间
     *
     * @param id id
     */
    public void updateTime(Integer id) {
        Questionnaire questionnaire = getById(id);
        questionnaire.setUpdateTime(new Date());
        baseMapper.updateById(questionnaire);
    }

    /**
     * 通过类型获取
     *
     * @param type 类型
     *
     * @return List<Questionnaire>
     */
    public Questionnaire getByType(Integer type) {
        LambdaQueryWrapper<Questionnaire> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Questionnaire::getType, type)
                .orderByAsc(Questionnaire::getCreateTime);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 表格
     *
     * @param questionResponse 返回值
     * @param tables           表格
     */
    private void setTable(QuestionResponse questionResponse, List<Table> tables) {
        List<InfectiousDiseaseTable> tableList = Lists.newArrayList();
        for (Table t : tables) {
            List<TableItem> item = t.getItem();
            String name = item.get(0).getName();
            if (StringUtils.equals(name, QuestionnaireConstant.INFECTIOUS_DISEASE_ONE)) {
                setInfectiousDiseaseTable(tableList, item, QuestionnaireConstant.INFECTIOUS_DISEASE_ONE);
            }
            if (StringUtils.equals(name, QuestionnaireConstant.INFECTIOUS_DISEASE_TWO)) {
                setInfectiousDiseaseTable(tableList, item, QuestionnaireConstant.INFECTIOUS_DISEASE_TWO);
            }
        }
        questionResponse.setInfectiousDiseaseTable(tableList);
    }

    /**
     * 设置疾病表格
     */
    private static void setInfectiousDiseaseTable(List<InfectiousDiseaseTable> tableList, List<TableItem> item, String name) {
        InfectiousDiseaseTable infectiousDiseaseTable = new InfectiousDiseaseTable();
        infectiousDiseaseTable.setName(QuestionnaireConstant.INFECTIOUS_DISEASE_PREFIX + name);
        if (StringUtils.equals(name, QuestionnaireConstant.INFECTIOUS_DISEASE_ONE)) {
            infectiousDiseaseTable.setDropSelect(DropSelectUtil.one());
        }
        if (StringUtils.equals(name, QuestionnaireConstant.INFECTIOUS_DISEASE_TWO)) {
            infectiousDiseaseTable.setDropSelect(DropSelectUtil.two());
        }

        List<List<TableItem>> lists = Lists.partition(item.stream().filter(s -> !StringUtils.equals(s.getName(), name)).collect(Collectors.toList()), 2);
        List<InfectiousDiseaseTable.Detail> collect = lists.stream().map(s -> {
            InfectiousDiseaseTable.Detail detail1 = new InfectiousDiseaseTable.Detail();
            detail1.setTableItems(s);
            return detail1;
        }).collect(Collectors.toList());
        infectiousDiseaseTable.setDetails(collect);
        tableList.add(infectiousDiseaseTable);
    }

}

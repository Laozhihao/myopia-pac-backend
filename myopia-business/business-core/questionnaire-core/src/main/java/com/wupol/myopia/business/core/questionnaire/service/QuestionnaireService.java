package com.wupol.myopia.business.core.questionnaire.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Simple4H
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
        // 删除问卷中的page_json
        Questionnaire questionnaire = getById(questionnaireId);
        questionnaire.setPageJson(null);
        updateById(questionnaire);
    }

    /**
     * 获取问卷问题
     *
     * @param questionnaireId 问卷Id
     */
    public List<QuestionnaireInfoDTO> getQuestionnaire(Integer questionnaireId) {
        Questionnaire questionnaire = this.getById(questionnaireId);
        Assert.notNull(questionnaire, "问卷不存在！");
        if (CollUtil.isNotEmpty(questionnaire.getPageJson())) {
            return questionnaire.getPageJson();
        }
        //如果没有页面数据，组装问卷数据
        List<QuestionnaireInfoDTO> questionnaireInfo = getQuestionnaireInfo(questionnaireId, false, true);
//        this.updateById(Questionnaire.builder().pageJson(questionnaireInfo).id(questionnaireId).build());
        return questionnaireInfo;
    }


    /**
     * 组装问卷问题数据
     *
     * @return List<QuestionnaireInfoDTO>
     */
    public List<QuestionnaireInfoDTO> getQuestionnaireInfo(Integer questionnaireId, Boolean isShowAll, Boolean isShowTable) {
        ArrayList<QuestionnaireInfoDTO> infoDTOS = Lists.newArrayList();
        List<QuestionnaireQuestion> questionnaireQuestions = questionnaireQuestionService.listByQuestionnaireId(questionnaireId);
        if (CollUtil.isEmpty(questionnaireQuestions)) {
            return Collections.emptyList();
        }
        List<Integer> questionIds = questionnaireQuestions.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
        List<Question> questions = questionService.listByIds(questionIds);
        Map<Integer, Question> questionMap = questions.stream().collect(Collectors.toMap(Question::getId, Function.identity()));

        List<Integer> tableQuestionIds = tableQuestionIds(questionnaireId, isShowAll);
        //过滤出顶层区域
        List<QuestionnaireQuestion> partLists = questionnaireQuestions.stream().filter(it -> QuestionnaireQuestion.TOP_PARENT_ID == it.getPid()).sorted(Comparator.comparing(QuestionnaireQuestion::getSort)).collect(Collectors.toList());
        partLists.forEach(it -> {
            Question question = questionMap.get(it.getQuestionId());
            QuestionnaireInfoDTO questionnaireInfoDTO = BeanCopyUtil.copyBeanPropertise(question, QuestionnaireInfoDTO.class);
            questionnaireInfoDTO.setExId(it.getId());
            questionnaireInfoDTO.setExPid(it.getPid());
            questionnaireInfoDTO.setIsNotShowNumber(it.getIsNotShowNumber());
            questionnaireInfoDTO.setSerialNumber(it.getSerialNumber());
            questionnaireInfoDTO.setIsLogic(it.getIsLogic());
            questionnaireInfoDTO.setJumpIds(it.getJumpIds());
            questionnaireInfoDTO.setIsHidden(it.getIsHidden());
            questionnaireInfoDTO.setRequired(it.getRequired());
            questionnaireInfoDTO.setQesData(it.getQesData());
            List<QuestionResponse> questionList = Lists.newArrayList();
            List<QuestionnaireQuestion> collect;

            if (Boolean.FALSE.equals(isShowAll)) {
                collect = questionnaireQuestions.stream().filter(s -> !tableQuestionIds.contains(s.getId())).collect(Collectors.toList());
            } else {
                collect = questionnaireQuestions;
            }
            //构建此模块下的所有问题
            collect.forEach(child -> {
                if (it.getId().equals(child.getPid())) {
                    Question childQuestion = questionMap.get(child.getQuestionId());
                    QuestionResponse questionResponse = commonBuildQuestion(childQuestion, child, questionMap, isShowTable);
                    buildQuestion(questionResponse, child.getId(), collect, questionMap, isShowTable);
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
     * @param pid           父Pid
     * @param childQuestion 孩子问题
     * @param questionMap   问题Map
     */
    protected void buildQuestion(QuestionResponse questionResponse, Integer pid, List<QuestionnaireQuestion> childQuestion,
                                 Map<Integer, Question> questionMap, Boolean isShowTable) {
        childQuestion.forEach(it -> {
            if (pid.equals(it.getPid())) {
                Question question = questionMap.get(it.getQuestionId());
                QuestionResponse childQuestionResponse = commonBuildQuestion(question, it, questionMap, isShowTable);
                List<QuestionResponse> questionResponses = CollUtil.isNotEmpty(questionResponse.getQuestionList()) ? questionResponse.getQuestionList() : new ArrayList<>();
                questionResponses.add(childQuestionResponse);
                questionResponse.setQuestionList(questionResponses);
                buildQuestion(childQuestionResponse, it.getId(), childQuestion, questionMap, isShowTable);
            }
        });
    }

    /***
     * 封装问题公共逻辑块
     * @param question 问题
     * @param it 问卷中间表
     * @param isShowTable 是否展示表格
     * @return QuestionResponse
     */
    public QuestionResponse commonBuildQuestion(Question question, QuestionnaireQuestion it, Map<Integer, Question> questionMap, Boolean isShowTable) {
        QuestionResponse childQuestionResponse = BeanCopyUtil.copyBeanPropertise(question, QuestionResponse.class);
        childQuestionResponse.setTitle(specialTitleProcess(childQuestionResponse.getTitle()));
        childQuestionResponse.setRequired(it.getRequired());
        childQuestionResponse.setSerialNumber(it.getSerialNumber());
        childQuestionResponse.setExId(it.getId());
        childQuestionResponse.setExPid(it.getPid());
        childQuestionResponse.setIsNotShowNumber(it.getIsNotShowNumber());
        childQuestionResponse.setIsLogic(it.getIsLogic());
        childQuestionResponse.setJumpIds(it.getJumpIds());
        childQuestionResponse.setIsHidden(it.getIsHidden());
        childQuestionResponse.setQesData(it.getQesData());
        setJumpIds(childQuestionResponse, it.getJumpIds());
        if (Boolean.TRUE.equals(isShowTable)) {
            if (StringUtils.equals(question.getType(), QuestionnaireConstant.INFECTIOUS_DISEASE_TITLE)) {
                setInfectiousDiseaseTitle(childQuestionResponse, it);
            }
            if (StringUtils.equals(question.getType(), QuestionnaireConstant.SCHOOL_CLASSROOM_TITLE)) {
                setSchoolClassroom(childQuestionResponse, it, questionMap);
            }
            if (StringUtils.equals(question.getType(), QuestionnaireConstant.TEACHER_TABLE)) {
                setSchoolTeacher(childQuestionResponse, it, questionMap);
            }
        }

        return childQuestionResponse;
    }

    /**
     * 封装跳转Id
     *
     * @param questionResponse 返回
     * @param jumpIdsDO        跳转题
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
        responseDTO.setDetail(getQuestionnaireInfo(id, true, false));
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
        queryWrapper.eq(Questionnaire::getType, type).orderByAsc(Questionnaire::getCreateTime);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 表格
     *
     * @param questionResponse 返回值
     */
    private void setInfectiousDiseaseTitle(QuestionResponse questionResponse, QuestionnaireQuestion it) {
        List<InfectiousDiseaseTable> tableList = Lists.newArrayList();
        List<QuestionnaireQuestion> temp = questionnaireQuestionService.findByList(new QuestionnaireQuestion().setQuestionnaireId(it.getQuestionnaireId()).setPid(it.getId()));
        if (CollUtil.isEmpty(temp) || temp.size() < 2) {
            return;
        }
        QuestionnaireQuestion q1 = temp.get(0);
        tableList.add(getInfectiousDiseaseTable(q1, QuestionnaireConstant.INFECTIOUS_DISEASE_PREFIX + QuestionnaireConstant.INFECTIOUS_DISEASE_ONE));
        QuestionnaireQuestion q2 = temp.get(1);
        tableList.add(getInfectiousDiseaseTable(q2, QuestionnaireConstant.INFECTIOUS_DISEASE_PREFIX + QuestionnaireConstant.INFECTIOUS_DISEASE_TWO));
        questionResponse.setInfectiousDiseaseTable(tableList);
    }

    /**
     * 传染病表格
     */
    private InfectiousDiseaseTable getInfectiousDiseaseTable(QuestionnaireQuestion q1, String name) {
        InfectiousDiseaseTable table = new InfectiousDiseaseTable();
        table.setName(name);

        List<QuestionnaireQuestion> questionList = questionnaireQuestionService.findByList(new QuestionnaireQuestion().setQuestionnaireId(q1.getQuestionnaireId()).setPid(q1.getId()));
        List<Integer> questionIds = questionList.stream().map(QuestionnaireQuestion::getQuestionId).collect(Collectors.toList());
        if (CollUtil.isEmpty(questionIds)) {
            return table;
        }
        List<Question> questions = questionService.listByIds(questionIds);
        List<InfectiousDiseaseTable.Detail> collect = questions.stream().map(question -> {
            InfectiousDiseaseTable.Detail detail = new InfectiousDiseaseTable.Detail();
            List<Option> options = question.getOptions();
            detail.setTableItems(options.stream().map(y -> {
                JSONObject option = y.getOption();
                return getTableItem(JSON.parseObject(JSON.toJSONString(option.get(String.valueOf(1))), JSONObject.class), new TableItem(), question.getId());
            }).collect(Collectors.toList()));
            return detail;
        }).collect(Collectors.toList());
        table.setTableItems(collect);
        return table;
    }

    /**
     * 表格
     *
     * @param questionResponse 返回值
     */
    private void setSchoolClassroom(QuestionResponse questionResponse, QuestionnaireQuestion it, Map<Integer, Question> questionMap) {
        List<QuestionnaireQuestion> questionnaireQuestionList = questionnaireQuestionService.findByList(new QuestionnaireQuestion().setQuestionnaireId(it.getQuestionnaireId()).setPid(it.getId()));
        if (CollUtil.isEmpty(questionnaireQuestionList)) {
            return;
        }
        List<ClassroomItemTable> tables = questionnaireQuestionList.stream().map(s -> {
            ClassroomItemTable table = new ClassroomItemTable();
            table.setName(questionMap.get(s.getQuestionId()).getTitle());
            table.setQuestionId(s.getQuestionId());
            List<QuestionnaireQuestion> nextList = questionnaireQuestionService.findByList(new QuestionnaireQuestion().setQuestionnaireId(s.getQuestionnaireId()).setPid(s.getId()));

            List<ClassroomItemTable.Detail> collect = nextList.stream().map(y -> {
                List<TableItem> tableItems = new ArrayList<>();
                Question question = questionMap.get(y.getQuestionId());
                Option option = question.getOptions().get(0);
                String text = option.getText();
                String[] split = text.split("-");
                JSONObject jsonObject = option.getOption();
                for (int i = 1; i <= jsonObject.size(); i++) {
                    TableItem tableItem = new TableItem();
                    if (i > 1) {
                        tableItem.setName(StringUtils.substringAfter(split[i - 1], "}"));
                    } else {
                        tableItem.setName(split[i - 1]);
                    }
                    tableItems.add(getTableItem(JSON.parseObject(JSON.toJSONString(jsonObject.get(String.valueOf(i))), JSONObject.class), tableItem, question.getId()));
                }
                return new ClassroomItemTable.Detail(question.getTitle().split("-")[0], tableItems);
            }).collect(Collectors.toList());

            List<ClassroomItemTable.Info> result = Lists.partition(collect, 3).stream()
                    .map(ClassroomItemTable.Info::new).collect(Collectors.toList());

            table.setTableItems(result);
            return table;
        }).collect(Collectors.toList());

        questionResponse.setClassroomItemTables(tables);
    }

    /**
     * 表格
     *
     * @param questionResponse 返回值
     */
    private void setSchoolTeacher(QuestionResponse questionResponse, QuestionnaireQuestion it, Map<Integer, Question> questionMap) {
        List<QuestionnaireQuestion> questionnaireQuestionList = questionnaireQuestionService.findByList(new QuestionnaireQuestion().setQuestionnaireId(it.getQuestionnaireId()).setPid(it.getId()));
        if (CollUtil.isEmpty(questionnaireQuestionList)) {
            return;
        }
        List<SchoolTeacherTable> collect = questionnaireQuestionList.stream().map(s -> {
            SchoolTeacherTable table = new SchoolTeacherTable();

            Question question = questionMap.get(s.getQuestionId());
            JSONObject option = question.getOptions().get(0).getOption();
            List<TableItem> tableItems = new ArrayList<>();
            for (int i = 1; i <= option.size(); i++) {
                tableItems.add(getTableItem(JSON.parseObject(JSON.toJSONString(option.get(String.valueOf(i))), JSONObject.class), new TableItem(), question.getId()));
            }
            table.setTableItems(tableItems);
            return table;
        }).collect(Collectors.toList());
        questionResponse.setSchoolTeacherTables(collect);
    }

    /**
     * 获取表格详情
     *
     * @param json       json
     * @param item       item
     * @param questionId 问题Id
     *
     * @return TableItem
     */
    private TableItem getTableItem(JSONObject json, TableItem item, Integer questionId) {
        item.setId(String.valueOf(json.getString(QuestionnaireConstant.ID)));
        item.setType(String.valueOf(json.get(QuestionnaireConstant.DATA_TYPE)));
        item.setDropSelectKey(String.valueOf(json.getString(QuestionnaireConstant.DROP_SELECT_KEY)));
        item.setQuestionId(questionId);
        item.setRequired(json.getBoolean(QuestionnaireConstant.REQUIRED));
        item.setFrontMark(-1);
        item.setOption(new TableItem.Option(json.getInteger(QuestionnaireConstant.MAX_LIMIT),
                json.getInteger(QuestionnaireConstant.MIN_LIMIT),
                json.getInteger(QuestionnaireConstant.RANGE),
                json.getInteger(QuestionnaireConstant.LENGTH)));
        return item;
    }

    /**
     * 需要过滤的题目
     */
    private List<Integer> tableQuestionIds(Integer questionnaireId, Boolean isShowAll) {
        if (Boolean.TRUE.equals(isShowAll)) {
            return new ArrayList<>();
        }
        ArrayList<String> tableTypes = Lists.newArrayList(QuestionnaireConstant.INFECTIOUS_DISEASE_TITLE, QuestionnaireConstant.SCHOOL_CLASSROOM_TITLE, QuestionnaireConstant.TEACHER_TABLE);
        List<Question> questions = questionService.getByTypes(tableTypes);
        List<Integer> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        List<QuestionnaireQuestion> questionnaireQuestionList = questionnaireQuestionService.getByQuestionnaireIdAndQuestionIds(questionnaireId, questionIds);
        List<Integer> ids = questionnaireQuestionList.stream().map(QuestionnaireQuestion::getId).collect(Collectors.toList());
        return getAllFilterId(ids, new ArrayList<>());
    }

    /**
     * 获取需要过滤的Id
     */
    private List<Integer> getAllFilterId(List<Integer> ids, List<Integer> result) {
        if (CollUtil.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<QuestionnaireQuestion> byPids = questionnaireQuestionService.getByPids(ids);
        List<Integer> temp = byPids.stream().map(QuestionnaireQuestion::getId).collect(Collectors.toList());
        result.addAll(temp);
        if (CollUtil.isNotEmpty(temp)) {
            getAllFilterId(temp, result);
        }
        return result;
    }

    /**
     * 特殊标题处理
     *
     * @param str 字符
     *
     * @return 字符
     */
    private String specialTitleProcess(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        if (StringUtils.contains(str, "||")) {
            return StringUtils.substringAfter(str, "||");
        }
        return str;
    }

}

package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wupol.myopia.business.aggregation.export.excel.domain.GenerateDataCondition;
import com.wupol.myopia.business.aggregation.export.excel.domain.GenerateRecDataBO;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireStatusEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.questionnaire.domain.dos.HideQuestionRecDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.Option;
import com.wupol.myopia.business.core.questionnaire.domain.dos.OptionAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.dos.RecDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.model.*;
import com.wupol.myopia.business.core.questionnaire.facade.QuestionnaireFacade;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户问卷答案
 *
 * @author hang.yuan 2022/7/21 19:50
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserAnswerRecFacade {

    private final UserQuestionRecordService userQuestionRecordService;
    private final UserAnswerService userAnswerService;
    private final QuestionService questionService;
    private final SchoolService schoolService;
    private final DistrictService districtService;
    private final ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    private final QuestionnaireFacade questionnaireFacade;
    private final QuestionnaireExcelFactory questionnaireExcelFactory;

    private static final String  ID = "id";
    private static final String  RADIO = "radio";
    private static final String  INPUT = "input";
    private static final String  CHECKBOX = "checkbox";
    private static final String  FILE_NAME="%s的%s的问卷数据.xlsx";
    private static final String  GRADE="年级";
    private static final String  CODE="编码4位";


    /**
     * 获取条件值（通知ID,任务ID,计划ID）
     * @param exportCondition 导出条件
     */
    private List<Integer> getConditionValue(ExportCondition exportCondition){
        Optional<ExportType> exportTypeService = questionnaireExcelFactory.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()){
            ExportType exportType = exportTypeService.get();
            return exportType.getConditionValue(exportCondition);
        }
        return defaultValue(null,null,null);
    }

    /**
     * 获取条件值的默认值
     * @param noticeId 通知ID
     * @param taskId 任务ID
     * @param planId 计划ID
     */
    public static List<Integer> defaultValue(Integer noticeId,Integer taskId,Integer planId) {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(noticeId);
        list.add(taskId);
        list.add(planId);
        return list;
    }
    /**
     * 获取问卷记录数（有数据的问卷 状态进行中或者已完成）
     *
     * @param exportCondition 导出条件
     * @param questionnaireTypeList 问卷类型集合
     */
    public List<UserQuestionRecord> getQuestionnaireRecordList(ExportCondition exportCondition, List<Integer> questionnaireTypeList,List<Integer> gradeTypeList) {
        List<Integer> conditionValue = getConditionValue(exportCondition);
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getListByNoticeIdOrTaskIdOrPlanId(conditionValue.get(0),conditionValue.get(1),conditionValue.get(2),QuestionnaireStatusEnum.FINISH.getCode());
        if (CollectionUtils.isEmpty(userQuestionRecordList)){
            return Lists.newArrayList();
        }

        Stream<UserQuestionRecord> userQuestionRecordStream = userQuestionRecordList.stream()
                .filter(userQuestionRecord -> questionnaireTypeList.contains(userQuestionRecord.getQuestionnaireType()));
        List<UserQuestionRecord> collect;
        if (Objects.nonNull(exportCondition.getSchoolId())){
            collect = userQuestionRecordStream
                    .filter(userQuestionRecord -> Objects.equals(userQuestionRecord.getSchoolId(),exportCondition.getSchoolId()))
                    .collect(Collectors.toList());
        } else {
            collect = userQuestionRecordStream.collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(collect)){
            return Lists.newArrayList();
        }

        Set<Integer> planStudentIds = collect.stream().map(UserQuestionRecord::getUserId).collect(Collectors.toSet());
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(planStudentIds));

        Set<Integer> districtIdList = Sets.newHashSet();
        if(Objects.nonNull(exportCondition.getDistrictId())){
            List<Integer> districtIds = districtService.getSpecificDistrictTreeAllDistrictIds(exportCondition.getDistrictId());
            districtIdList.addAll(districtIds);
            if (!districtIds.contains(exportCondition.getDistrictId())){
                districtIdList.add(exportCondition.getDistrictId());
            }
        }

        Stream<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentStream = planSchoolStudentList.stream().filter(planSchoolStudent -> gradeTypeList.contains(planSchoolStudent.getGradeType()));
        //有数据过滤
        if (!CollectionUtils.isEmpty(districtIdList)){
            screeningPlanSchoolStudentStream = screeningPlanSchoolStudentStream.filter(planSchoolStudent-> districtIdList.contains(planSchoolStudent.getSchoolDistrictId()));
        }
        List<Integer> planStudentIdList = screeningPlanSchoolStudentStream .map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toList());

        return collect.stream()
                .filter(userQuestionRecord -> planStudentIdList.contains(userQuestionRecord.getUserId()))
                .collect(Collectors.toList());

    }

    /**
     * rec导出数据处理
     *
     * @param userQuestionRecordList 用户问答记录集合
     * @param hideQuestionRecDataBOList 隐藏问题数据集合
     * @param recDataBOList 收集rec导出数据集合
     */
    private void recDataProcess(List<UserQuestionRecord> userQuestionRecordList,
                                List<HideQuestionRecDataBO> hideQuestionRecDataBOList,
                                List<RecDataBO> recDataBOList) {

        if (CollectionUtils.isEmpty(userQuestionRecordList)){
            return;
        }

        List<Integer> recordIds = userQuestionRecordList.stream().map(UserQuestionRecord::getId).collect(Collectors.toList());
        List<UserAnswer> userAnswerList = userAnswerService.getListByRecordIds(recordIds);

        //用户记录ID对应答案集合
        Map<Integer, List<UserAnswer>> userAnswerMap = userAnswerList.stream().collect(Collectors.groupingBy(UserAnswer::getRecordId));

        Set<Integer> questionIds = userAnswerList.stream().map(UserAnswer::getQuestionId).collect(Collectors.toSet());
        List<Question> questionList = questionService.listByIds(questionIds);
        //问题ID对应的问题集合
        Map<Integer, Question> questionMap = questionList.stream().collect(Collectors.toMap(Question::getId, Function.identity()));

        //学生ID对应学生信息（年级和编码4位）集合
        Map<Integer, TwoTuple<String,String>> studentInfoMap = getStudentInfoMap(userQuestionRecordList);

        //学生ID对应的问卷记录信息集合
        Map<Integer, List<UserQuestionRecord>> studentMap = userQuestionRecordList.stream().collect(Collectors.groupingBy(UserQuestionRecord::getStudentId));

        studentMap.forEach((studentId,recordList)->{
            RecDataBO recDataBO = new RecDataBO();
            recDataBO.setStudentId(studentId);
            Date fillDate = recordList.stream().max(Comparator.comparing(UserQuestionRecord::getUpdateTime)).map(UserQuestionRecord::getUpdateTime).orElse(new Date());
            TwoTuple<String, String> tuple = studentInfoMap.get(studentId);
            recDataBO.setGradeCode(GradeCodeEnum.getByName(tuple.getFirst()).getCode());
            for (UserQuestionRecord userQuestionRecord : recordList) {
                if (Objects.equals(userQuestionRecord.getQuestionnaireType(), QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType())
                        || Objects.equals(userQuestionRecord.getQuestionnaireType(), QuestionnaireTypeEnum.VISION_SPINE_NOTICE.getType())){
                    //处理隐藏数据（学生和学校数据）
                    List<RecDataBO.RecAnswerDataBO> recAnswerDataBOList = hideQuestionRecDataProcess(userQuestionRecord.getUserId(), fillDate, hideQuestionRecDataBOList);
                    recDataBO.setRecAnswerDataBOList(recAnswerDataBOList);
                }
                //处理非隐藏数据
                questionRecDataProcess(userAnswerMap, questionMap, recDataBO, userQuestionRecord.getId(),tuple);
            }
            recDataBOList.add(recDataBO);
        });

    }

    /**
     * 获取学生信息(年级和编码4位)
     * @param userQuestionRecordList 用户问卷记录集合
     */
    private Map<Integer, TwoTuple<String,String>> getStudentInfoMap(List<UserQuestionRecord> userQuestionRecordList) {
        Set<Integer> planStudentIds = userQuestionRecordList.stream().map(UserQuestionRecord::getUserId).collect(Collectors.toSet());
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(planStudentIds));
        return planSchoolStudentList.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getStudentId, screeningPlanSchoolStudent -> {
            String code = StrUtil.EMPTY;
            String commonDiseaseId = screeningPlanSchoolStudent.getCommonDiseaseId();
            if (StrUtil.isNotBlank(commonDiseaseId)){
                code = commonDiseaseId.substring(commonDiseaseId.length() - 4);
            }
            return TwoTuple.of(screeningPlanSchoolStudent.getGradeName(),code);
        }));
    }

    /**
     * 处理非隐藏数据
     * @param userAnswerMap 用户答案集合
     * @param questionMap 问题集合
     * @param recDataBO 导出excel数据对象
     * @param userQuestionRecordId 用户问卷记录ID
     * @param tuple 学生信息(年级和编码4位)
     */
    private void questionRecDataProcess(Map<Integer, List<UserAnswer>> userAnswerMap, Map<Integer, Question> questionMap, RecDataBO recDataBO, Integer userQuestionRecordId,TwoTuple<String, String> tuple) {
        List<UserAnswer> userAnswers = userAnswerMap.get(userQuestionRecordId);
        if (CollUtil.isNotEmpty(userAnswers)){
            Map<Integer, List<UserAnswer>> questionUserAnswerMap  = userAnswers.stream().collect(Collectors.groupingBy(UserAnswer::getQuestionId));
            List<RecDataBO.RecAnswerDataBO> answerList = Lists.newArrayList();
            questionUserAnswerMap.forEach((questionId,list)-> getRecAnswerData(list, questionMap,tuple,answerList));
            if (Objects.isNull(recDataBO.getRecAnswerDataBOList())) {
                recDataBO.setRecAnswerDataBOList(answerList);
            }else {
                recDataBO.getRecAnswerDataBOList().addAll(answerList);
            }
        }
    }


    /**
     * 隐藏问卷数据处理
     * @param planStudentId 计划学生ID
     * @param fillDate 填写日期
     * @param hideQuestionDataBOList 隐藏问题数据集合
     */
    private List<RecDataBO.RecAnswerDataBO> hideQuestionRecDataProcess(Integer planStudentId ,Date fillDate,
                                                                          List<HideQuestionRecDataBO> hideQuestionDataBOList){
        List<RecDataBO.RecAnswerDataBO> answerDataBOList =Lists.newArrayList();

        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getById(planStudentId);
        School school = schoolService.getById(screeningPlanSchoolStudent.getSchoolId());

        String commonDiseaseId = screeningPlanSchoolStudent.getCommonDiseaseId();

        for (int i = 0; i < hideQuestionDataBOList.size(); i++) {
            HideQuestionRecDataBO hideQuestionDataBO = hideQuestionDataBOList.get(i);
            RecDataBO.RecAnswerDataBO recAnswerDataBO = new RecDataBO.RecAnswerDataBO();
            List<HideQuestionRecDataBO.QesDataBO> qesDataList = hideQuestionDataBO.getQesData();
            qesDataList = qesDataList.stream().filter(qesDataDO -> !Objects.equals(qesDataDO.getQesField(), "QM")).collect(Collectors.toList());
            if (CollUtil.isEmpty(qesDataList)){
                continue;
            }
            if (Objects.equals(hideQuestionDataBO.getType(),INPUT)){
                HideQuestionRecDataBO.QesDataBO qesDataBO = qesDataList.get(0);
                recAnswerDataBO.setQesField(qesDataBO.getQesField());
                switch (qesDataBO.getQesField()){
                    case "province":
                        recAnswerDataBO.setRecAnswer(commonDiseaseId.substring(0, 2));
                        break;
                    case "city":
                        recAnswerDataBO.setRecAnswer(commonDiseaseId.substring(2, 4));
                        break;
                    case "district":
                        recAnswerDataBO.setRecAnswer(commonDiseaseId.substring(4,5));
                        break;
                    case "county":
                        recAnswerDataBO.setRecAnswer(commonDiseaseId.substring(5, 7));
                        break;
                    case "point":
                        recAnswerDataBO.setRecAnswer(commonDiseaseId.substring(7,8));
                        break;
                    case "school":
                        recAnswerDataBO.setRecAnswer(commonDiseaseId.substring(8,10));
                        break;
                    case "a01":
                        recAnswerDataBO.setRecAnswer(commonDiseaseId.substring(10,12));
                        break;
                    case "a011":
                        recAnswerDataBO.setRecAnswer(commonDiseaseId.substring(12,16));
                        break;
                    case "ID1":
                    case "ID2":
                        recAnswerDataBO.setRecAnswer(commonDiseaseId);
                        break;
                    case "date":
                        recAnswerDataBO.setRecAnswer(DateUtil.format(fillDate, "yyyy/MM/dd"));
                        break;

                    default:
                        break;
                }

            }
            if (Objects.equals(hideQuestionDataBO.getType(),RADIO)){
                HideQuestionRecDataBO.QesDataBO qesDataBO = qesDataList.get(0);
                if (Objects.equals(qesDataBO.getQesField(),"a02")){
                    recAnswerDataBO.setQesField(qesDataBO.getQesField());
                    recAnswerDataBO.setRecAnswer(getGenderRecData(screeningPlanSchoolStudent.getGender()));
                }
            }
            answerDataBOList.add(recAnswerDataBO);
        }
        return answerDataBOList;
    }

    /**
     * 获取性别的rec数据
     * @param gender 性别
     */
    private String getGenderRecData(Integer gender){
        if (Objects.equals(gender,0)){
            return "1";
        }
        if (Objects.equals(gender,1)){
            return "2";
        }
        return StrUtil.EMPTY;
    }


    /**
     * 获取答案数据
     * @param userAnswerList 用户答案数据集合
     * @param questionMap 问题集合
     * @param tuple 学生信息(年级和编码4位)
     */
    private void getRecAnswerData(List<UserAnswer> userAnswerList,Map<Integer, Question> questionMap,TwoTuple<String, String> tuple,List<RecDataBO.RecAnswerDataBO> answerDataBOList){
        UserAnswer userAnswer = userAnswerList.get(0);
        Question question = questionMap.get(userAnswer.getQuestionId());

        List<Option> options = JSON.parseArray(JSON.toJSONString(question.getOptions()), Option.class);
        if (options.size() == 1){
            setDataInputType(userAnswerList, answerDataBOList, question, options,tuple);
        }else {
            //处理 多选或者单选
            Map<String, Option> optionMap = options.stream().collect(Collectors.toMap(Option::getId, Function.identity()));
            if (Objects.equals(question.getType(), CHECKBOX) || Objects.equals(question.getType(), RADIO)) {
                setAnswerData(answerDataBOList, userAnswerList, optionMap);
            }
        }
    }

    /**
     * 根据输入框类型设置答案
     * @param userAnswerList 用户答案集合
     * @param answerDataBOList 用户答案数据
     * @param question 问题
     * @param options 问题选项
     * @param tuple 学生信息(年级和编码4位)
     */
    private void setDataInputType(List<UserAnswer> userAnswerList,List<RecDataBO.RecAnswerDataBO> answerDataBOList, Question question, List<Option> options,TwoTuple<String, String> tuple) {
        Option questionOption = options.get(0);
        Map<String, OptionAnswer> optionAnswerMap = getStreamByOptionAnswerList(userAnswerList).collect(Collectors.toMap(OptionAnswer::getOptionId, Function.identity()));
        if (Objects.equals(question.getType(), RADIO)) {
            JSONObject option = questionOption.getOption();
            if (Objects.nonNull(option) && option.size() > 0 ){
                setRadioAnswer(answerDataBOList, optionAnswerMap, option);
            }else {
                OptionAnswer optionAnswer = optionAnswerMap.get(questionOption.getId());
                addAnswerData(answerDataBOList,optionAnswer);
            }
        }
        else if (Objects.equals(question.getType(), INPUT)) {
            getStreamByOptionAnswerList(userAnswerList).forEach(optionAnswer -> {
                addAnswerData(answerDataBOList, optionAnswer);
            });
        }
    }

    private void addAnswerData(List<RecDataBO.RecAnswerDataBO> answerDataBOList, OptionAnswer optionAnswer) {
        if (Objects.isNull(optionAnswer)){
            return;
        }
        String resultValue;
        if (StrUtil.isNotBlank(optionAnswer.getQesSerialNumber())){
            resultValue = optionAnswer.getQesSerialNumber();
        }else {
            resultValue = optionAnswer.getValue();
        }
        RecDataBO.RecAnswerDataBO recAnswerDataBO = new RecDataBO.RecAnswerDataBO(optionAnswer.getQesField(),resultValue);
        answerDataBOList.add(recAnswerDataBO);
    }

    /**
     * 获取选项答案流
     * @param userAnswerList 用户答案集合
     */
    private Stream<OptionAnswer> getStreamByOptionAnswerList(List<UserAnswer> userAnswerList){
        return userAnswerList.stream().flatMap(answer -> {
            List<OptionAnswer> answerList = JSON.parseArray(JSON.toJSONString(answer.getAnswer()), OptionAnswer.class);
            return answerList.stream();
        });
    }

    /**
     * 设置 redio类型答案
     * @param optionAnswerMap 问题答案集合
     * @param option 问题里面的操作
     */
    private void setRadioAnswer(List<RecDataBO.RecAnswerDataBO> answerDataBOList, Map<String, OptionAnswer> optionAnswerMap, JSONObject option) {
        for (Map.Entry<String, Object> entry : option.entrySet()) {
            JSONObject value = (JSONObject) entry.getValue();
            OptionAnswer optionAnswer = optionAnswerMap.get(value.getString(ID));
            if (Objects.isNull(optionAnswer)){continue;}
            addAnswerData(answerDataBOList,optionAnswer);
        }
    }

    /**
     * 设置单选、多选、单选+输入框、多选+输入框
     * @param userAnswerList 用户答案
     * @param optionMap 选项集合
     */
    private void setAnswerData(List<RecDataBO.RecAnswerDataBO> answerDataBOList, List<UserAnswer> userAnswerList, Map<String, Option> optionMap) {
        Map<String,OptionAnswer> optionAnswerMap = getStreamByOptionAnswerList(userAnswerList).collect(Collectors.toMap(OptionAnswer::getOptionId,Function.identity()));
        List<OptionAnswer> optionAnswerList = getStreamByOptionAnswerList(userAnswerList).collect(Collectors.toList());

        for (OptionAnswer optionAnswer : optionAnswerList) {
            Option questionOption = optionMap.get(optionAnswer.getOptionId());
            if (Objects.isNull(questionOption)){
                continue;
            }
            JSONObject option = questionOption.getOption();
            if (Objects.nonNull(option) && option.size() > 0 ){
                // checkbox/radio 和input组合
                setCheckboxOrRadioAnswer(optionAnswerMap, answerDataBOList, option);
            }else {
                //checkbox/radio
                addAnswerData(answerDataBOList,optionAnswerMap.get(questionOption.getId()));
            }
        }
    }

    /**
     * 设置多选或者单选或输入框组合答案
     * @param optionAnswerMap 选项答案集合
     * @param answerDataBOList 答案集合
     * @param option 问题里面的选项操作
     */
    private void setCheckboxOrRadioAnswer(Map<String, OptionAnswer> optionAnswerMap, List<RecDataBO.RecAnswerDataBO> answerDataBOList, JSONObject option) {
        for (Map.Entry<String, Object> entry : option.entrySet()) {
            JSONObject json = JSON.parseObject(JSON.toJSONString(entry.getValue()), JSONObject.class);
            OptionAnswer optionAnswer = optionAnswerMap.get(json.getString(ID));
            if (Objects.isNull(optionAnswer)){
                continue;
            }
            addAnswerData(answerDataBOList,optionAnswer);
        }
    }

    /**
     * 获取导出Excel的数据
     * @param userQuestionRecordList 用户问卷记录集合
     * @param questionnaireIds 问卷ID集合
     * @param questionnaireId 隐藏问题问卷ID
     */
    public List<Map<String,String>> getRecData(List<UserQuestionRecord> userQuestionRecordList,
                                      List<Integer> questionnaireIds,Integer questionnaireId){
        List<RecDataBO> recDataBOList = Lists.newArrayList();
        List<HideQuestionRecDataBO> hideQuestionDataBOList = questionnaireFacade.getHideQuestionnaireQuestionRec(questionnaireId);
        recDataProcess(userQuestionRecordList,hideQuestionDataBOList, recDataBOList);
        for (HideQuestionRecDataBO hideQuestionDataBO : hideQuestionDataBOList) {
            System.out.println(hideQuestionDataBO);
        }
        List<QesFieldMapping> qesFieldMappingList = questionnaireFacade.getQesFieldMappingList(questionnaireIds);
        CollUtil.sort(recDataBOList,Comparator.comparing(RecDataBO::getGradeCode));
        for (RecDataBO recDataBO : recDataBOList) {
            List<RecDataBO.RecAnswerDataBO> dataList = recDataBO.getRecAnswerDataBOList();
            for (RecDataBO.RecAnswerDataBO answerDataBO : dataList) {
                System.out.println(answerDataBO);
            }
        }

        return Lists.newArrayList();
    }


    /**
     * 获取excel问卷名称
     *
     * @param schoolId 学校ID
     * @param questionnaireType 问卷类型
     */
    public String getExcelFileName(Integer schoolId,Integer questionnaireType){
        School school = schoolService.getById(schoolId);
        QuestionnaireTypeEnum questionnaireTypeEnum = QuestionnaireTypeEnum.getQuestionnaireType(questionnaireType);
        return String.format(FILE_NAME,school.getName(),questionnaireTypeEnum.getDesc());
    }


    /**
     * 获取rec数据
     * @param generateDataCondition 生成数据条件
     */
    public GenerateRecDataBO generateRecData(GenerateDataCondition generateDataCondition){
        QuestionnaireTypeEnum mainBodyType = generateDataCondition.getMainBodyType();
        ExportCondition exportCondition = generateDataCondition.getExportCondition();
        //根据问卷类型获取问卷集合
        List<Questionnaire> questionnaireList = questionnaireFacade.getLatestQuestionnaire(mainBodyType);
        if (CollUtil.isEmpty(questionnaireList)){
            log.warn("暂无此问卷类型：{}",mainBodyType.getDesc());
            return null;
        }

        //获取用户问卷记录
        List<UserQuestionRecord> userQuestionRecordList = getQuestionnaireRecordList(exportCondition, questionnaireFacade.getQuestionnaireTypeList(mainBodyType), generateDataCondition.getGradeTypeList());
        if (CollUtil.isEmpty(userQuestionRecordList)){
            log.info("暂无数据：notificationId:{}、planId:{}、taskId:{},问卷类型：{}",exportCondition.getNotificationId(),exportCondition.getPlanId(),exportCondition.getTaskId(),mainBodyType.getDesc());
            return null;
        }

        //获取学生类型问卷的 基础信息部分问卷ID
        Integer questionnaireId = questionnaireList.stream()
                .filter(questionnaire -> Objects.equals(questionnaire.getType(), generateDataCondition.getBaseInfoType().getType()))
                .findFirst().map(Questionnaire::getId).orElse(null);

        List<Integer> latestQuestionnaireIds;

        if (Objects.equals(Boolean.TRUE,generateDataCondition.getIsAsc())){
            latestQuestionnaireIds = questionnaireList.stream().sorted(Comparator.comparing(Questionnaire::getType)).map(Questionnaire::getId).collect(Collectors.toList());
        }else {
            latestQuestionnaireIds = questionnaireList.stream().sorted(Comparator.comparing(Questionnaire::getType).reversed()).map(Questionnaire::getId).collect(Collectors.toList());
        }

        Map<Integer, List<UserQuestionRecord>> schoolRecordMap = userQuestionRecordList.stream()
                .filter(userQuestionRecord -> latestQuestionnaireIds.contains(userQuestionRecord.getQuestionnaireId()))
                .sorted(Comparator.comparing(UserQuestionRecord::getId))
                .collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));

        return getRecData(latestQuestionnaireIds,questionnaireId,schoolRecordMap);

    }

    /**
     * 获取excel头信息和数据
     * @param latestQuestionnaireIds 最新问卷ID集合
     * @param questionnaireId 问卷基础部分对应的问卷ID
     * @param schoolRecordMap 学校对应用户记录集合
     */
    public GenerateRecDataBO getRecData(List<Integer> latestQuestionnaireIds,Integer questionnaireId,Map<Integer, List<UserQuestionRecord>> schoolRecordMap){
        GenerateRecDataBO generateRecDataBO = new GenerateRecDataBO();

        Map<Integer,List<Map<String,String>>> dataMap= Maps.newHashMap();
        for (Map.Entry<Integer, List<UserQuestionRecord>> entry : schoolRecordMap.entrySet()) {
            dataMap.put(entry.getKey(), getRecData(entry.getValue(), latestQuestionnaireIds,questionnaireId));
        }

        return generateRecDataBO;
    }
}

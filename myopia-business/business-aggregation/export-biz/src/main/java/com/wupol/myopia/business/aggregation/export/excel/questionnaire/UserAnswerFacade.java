package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.ExportQuestionnaireService;
import com.wupol.myopia.business.aggregation.export.excel.domain.GenerateExcelDataBO;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireStatusEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.questionnaire.domain.dos.ExcelStudentDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.HideQuestionDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.Option;
import com.wupol.myopia.business.core.questionnaire.domain.dos.OptionAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.school.constant.AreaTypeEnum;
import com.wupol.myopia.business.core.school.constant.MonitorTypeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class UserAnswerFacade {

    private final UserQuestionRecordService userQuestionRecordService;
    private final UserAnswerService userAnswerService;
    private final QuestionService questionService;
    private final SchoolService schoolService;
    private final DistrictService districtService;
    private final ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    private final QuestionnaireFacade questionnaireFacade;
    private final QuestionnaireExcelFactory questionnaireExcelFactory;

    private static final String  PLACEHOLDER = "-{%s}";
    private static final String  ID = "id";
    private static final String  RADIO = "radio";
    private static final String  INPUT = "input";
    private static final String  CHECKBOX = "checkbox";
    private static final String  FILE_NAME="%s的%s的问卷数据.xlsx";


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
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getListByNoticeIdOrTaskIdOrPlanId(conditionValue.get(0),conditionValue.get(1),conditionValue.get(2));
        if (!CollectionUtils.isEmpty(userQuestionRecordList)){
            Stream<UserQuestionRecord> userQuestionRecordStream = userQuestionRecordList.stream()
                    .filter(userQuestionRecord -> !Objects.equals(userQuestionRecord.getStatus(), QuestionnaireStatusEnum.NOT_START.getCode()))
                    .filter(userQuestionRecord -> questionnaireTypeList.contains(userQuestionRecord.getQuestionnaireType()));
            List<UserQuestionRecord> collect;
            if (Objects.nonNull(exportCondition.getSchoolId())){
                collect = userQuestionRecordStream
                        .filter(userQuestionRecord -> Objects.equals(userQuestionRecord.getSchoolId(),exportCondition.getSchoolId()))
                        .collect(Collectors.toList());
            } else {
                collect = userQuestionRecordStream.collect(Collectors.toList());
            }

            Set<Integer> planStudentIds = collect.stream().map(UserQuestionRecord::getUserId).collect(Collectors.toSet());
            List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(planStudentIds));
            List<Integer> planStudentIdList = planSchoolStudentList.stream()
                    .filter(planSchoolStudent -> gradeTypeList.contains(planSchoolStudent.getGradeType()))
                    .map(ScreeningPlanSchoolStudent::getId)
                    .collect(Collectors.toList());

            return collect.stream()
                    .filter(userQuestionRecord -> planStudentIdList.contains(userQuestionRecord.getUserId()))
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * excel导出数据处理
     *
     * @param userQuestionRecordList 用户问答记录集合
     * @param hideQuestionDataBOList 隐藏问题数据集合
     * @param excelStudentDataBOList 收集excel导出学生数据集合
     */
    private void dataProcess(List<UserQuestionRecord> userQuestionRecordList,
                            List<HideQuestionDataBO> hideQuestionDataBOList,
                            List<ExcelStudentDataBO> excelStudentDataBOList) {
        if (CollectionUtils.isEmpty(userQuestionRecordList)){
            return;
        }

        List<Integer> recordIds = userQuestionRecordList.stream().map(UserQuestionRecord::getId).collect(Collectors.toList());

        //记分问题ID
        List<Integer> questionnaireIds = userQuestionRecordList.stream().map(UserQuestionRecord::getQuestionnaireId).collect(Collectors.toList());
        List<Integer> scoreQuestionIds = questionnaireFacade.getScoreQuestionIds(questionnaireIds);

        List<UserAnswer> userAnswerList = userAnswerService.getListByRecordIds(recordIds);
        Map<Integer, List<UserAnswer>> userAnswerMap = userAnswerList.stream().collect(Collectors.groupingBy(UserAnswer::getRecordId));

        Set<Integer> questionIds = userAnswerList.stream().map(UserAnswer::getQuestionId).collect(Collectors.toSet());
        List<Question> questionList = questionService.listByIds(questionIds);
        Map<Integer, Question> questionMap = questionList.stream().collect(Collectors.toMap(Question::getId, Function.identity()));

        Map<Integer, List<UserQuestionRecord>> studentMap = userQuestionRecordList.stream().collect(Collectors.groupingBy(UserQuestionRecord::getStudentId));
        studentMap.forEach((studentId,recordList)->{
            ExcelStudentDataBO excelStudentDataBO = new ExcelStudentDataBO();
            excelStudentDataBO.setStudentId(studentId);
            Date fillDate = recordList.stream().max(Comparator.comparing(UserQuestionRecord::getUpdateTime)).map(UserQuestionRecord::getUpdateTime).orElse(new Date());

            for (UserQuestionRecord userQuestionRecord : recordList) {
                if (Objects.equals(userQuestionRecord.getQuestionnaireType(), QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType())
                        || Objects.equals(userQuestionRecord.getQuestionnaireType(), QuestionnaireTypeEnum.VISION_SPINE_NOTICE.getType())){
                    //处理隐藏数据（学生和学校数据）
                    List<ExcelStudentDataBO.AnswerDataBO> answerDataBOList = hideQuestionDataProcess(userQuestionRecord.getUserId(),fillDate,hideQuestionDataBOList);
                    excelStudentDataBO.setDataList(answerDataBOList);
                }
                //处理非隐藏数据
                List<UserAnswer> userAnswers = userAnswerMap.get(userQuestionRecord.getId());
                if (CollectionUtil.isNotEmpty(userAnswers)){
                    Map<Integer, List<UserAnswer>> questionUserAnswerMap  = userAnswers.stream().collect(Collectors.groupingBy(UserAnswer::getQuestionId));
                    List<ExcelStudentDataBO.AnswerDataBO> answerList =Lists.newArrayList();
                    List<ExcelStudentDataBO.AnswerDataBO> scoreAnswerList =Lists.newArrayList();

                    questionUserAnswerMap.forEach((questionId,list)-> {
                        ExcelStudentDataBO.AnswerDataBO answerData = getAnswerData(list, questionMap);
                        answerList.add(answerData);
                        if (scoreQuestionIds.contains(questionId)){
                            scoreAnswerList.add(answerData);
                        }
                    });

                    if (CollectionUtil.isNotEmpty(scoreAnswerList)){
                        int totalScore = scoreAnswerList.stream()
                                .map(answerDataBO -> {
                                    Question question = questionMap.get(answerDataBO.getQuestionId());
                                    return question.getOptions().stream()
                                            .filter(option -> Objects.equals(option.getText(), answerDataBO.getAnswer()))
                                            .findFirst().orElse(null);
                                })
                                .filter(Objects::nonNull)
                                .map(Option::getScoreValue)
                                .filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
                        answerList.add(new ExcelStudentDataBO.AnswerDataBO(-1,String.valueOf(totalScore)));
                    }

                    if (Objects.isNull(excelStudentDataBO.getDataList())) {
                        excelStudentDataBO.setDataList(answerList);
                    }else {
                        excelStudentDataBO.getDataList().addAll(answerList);
                    }
                }
            }
            excelStudentDataBOList.add(excelStudentDataBO);
        });

    }

    /**
     * 隐藏问卷数据处理
     * @param planStudentId 计划学生ID
     * @param fillDate 填写日期
     * @param hideQuestionDataBOList 隐藏问题数据集合
     */
    private List<ExcelStudentDataBO.AnswerDataBO> hideQuestionDataProcess(Integer planStudentId ,Date fillDate,
                                                                          List<HideQuestionDataBO> hideQuestionDataBOList){
        List<ExcelStudentDataBO.AnswerDataBO> answerDataBOList =Lists.newArrayList();

        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getById(planStudentId);
        School school = schoolService.getById(screeningPlanSchoolStudent.getSchoolId());
        List<String> districtList = getParseDistrict(school.getDistrictAreaCode());

        for (int i = 0; i < hideQuestionDataBOList.size(); i++) {
            ExcelStudentDataBO.AnswerDataBO answerDataBO = new ExcelStudentDataBO.AnswerDataBO();
            answerDataBO.setQuestionId(hideQuestionDataBOList.get(i).getQuestionId());
            switch (i){
                case 0:
                    answerDataBO.setAnswer(screeningPlanSchoolStudent.getCommonDiseaseId());
                    answerDataBOList.add(answerDataBO);
                    break;
                case 1:
                    answerDataBO.setAnswer(getDistrictName(districtList,0));
                    answerDataBOList.add(answerDataBO);
                    break;
                case 2:
                    answerDataBO.setAnswer(getDistrictName(districtList,1));
                    answerDataBOList.add(answerDataBO);
                    break;
                case 3:
                    answerDataBO.setAnswer(Optional.ofNullable(school.getAreaType()).map(type-> Optional.ofNullable(AreaTypeEnum.get(type)).map(AreaTypeEnum::getName).orElse(StrUtil.EMPTY)).orElse(StrUtil.EMPTY));
                    answerDataBOList.add(answerDataBO);
                    break;
                case 4:
                    answerDataBO.setAnswer(getDistrictName(districtList,2));
                    answerDataBOList.add(answerDataBO);
                    break;
                case 5:
                    answerDataBO.setAnswer(Optional.ofNullable(school.getMonitorType()).map(type-> Optional.ofNullable(MonitorTypeEnum.get(type)).map(MonitorTypeEnum::getName).orElse(StrUtil.EMPTY)).orElse(StrUtil.EMPTY));
                    answerDataBOList.add(answerDataBO);
                    break;
                case 6:
                    answerDataBO.setAnswer(school.getName());
                    answerDataBOList.add(answerDataBO);
                    break;
                case 7:
                    answerDataBO.setAnswer(DateUtil.format(fillDate, DatePattern.NORM_DATE_PATTERN));
                    answerDataBOList.add(answerDataBO);
                    break;
                default:
                    break;
            }
        }
        return answerDataBOList;
    }

    private List<String> getParseDistrict(Long districtAreaCode){
        if (Objects.isNull(districtAreaCode)){
            return Lists.newArrayList();
        }
        String code = districtAreaCode.toString();
        Map<Long,String> codeMap = Maps.newLinkedHashMap();
        codeMap.put(Long.parseLong(code.substring(0, 2))*10000000,"");
        codeMap.put(Long.parseLong(code.substring(0, 4))*100000,"");
        codeMap.put(Long.parseLong(code.substring(0, 6))*1000,"");
        codeMap.forEach((k,v)->{
            District district = districtService.getDistrictByCode(k, Boolean.FALSE);
            if (Objects.nonNull(district)){
                codeMap.put(k,district.getName());
            }
        });

        return Lists.newArrayList(codeMap.values());
    }

    private static String getDistrictName(List<String> districtList ,Integer index){
        return CollectionUtil.isNotEmpty(districtList) ? districtList.get(index):StrUtil.EMPTY;
    }

    /**
     * 获取答案数据
     * @param userAnswerList 用户答案数据集合
     * @param questionMap 问题集合
     */
    private ExcelStudentDataBO.AnswerDataBO getAnswerData(List<UserAnswer> userAnswerList,Map<Integer, Question> questionMap){
        ExcelStudentDataBO.AnswerDataBO answerDataBO = new ExcelStudentDataBO.AnswerDataBO();
        UserAnswer userAnswer = userAnswerList.get(0);
        answerDataBO.setQuestionId(userAnswer.getQuestionId());
        Question question = questionMap.get(userAnswer.getQuestionId());

        List<Option> options = JSONObject.parseArray(JSONObject.toJSONString(question.getOptions()), Option.class);
        if (options.size() == 1){
            Option questionOption = options.get(0);
            Map<String,OptionAnswer> optionAnswerMap = userAnswerList.stream().flatMap(answer->{
                List<OptionAnswer> answerList = JSONObject.parseArray(JSONObject.toJSONString(answer.getAnswer()), OptionAnswer.class);
                return answerList.stream();
            }).collect(Collectors.toMap(OptionAnswer::getOptionId,Function.identity()));

            if (Objects.equals(question.getType(), RADIO)) {

                JSONObject option = questionOption.getOption();
                if (Objects.nonNull(option) && option.size() > 0 ){
                    String answer = questionOption.getText();
                    for (Map.Entry<String, Object> entry : option.entrySet()) {
                        JSONObject value = (JSONObject) entry.getValue();
                        OptionAnswer optionAnswer = optionAnswerMap.get(value.getString(ID));
                        if (Objects.nonNull(optionAnswer)){
                            answer = answer.replace(String.format(PLACEHOLDER, entry.getKey()), Optional.ofNullable(optionAnswer.getValue()).orElse(StrUtil.EMPTY));
                        }
                    }
                    answerDataBO.setAnswer(answer);
                }else {
                    answerDataBO.setAnswer(questionOption.getText());
                }
            }
            else if (Objects.equals(question.getType(), INPUT)) {
                List<String> valueList = userAnswerList.stream().flatMap(answer -> {
                    List<OptionAnswer> answerList = JSONObject.parseArray(JSONObject.toJSONString(answer.getAnswer()), OptionAnswer.class);
                    return answerList.stream();
                }).map(optionAnswer -> Optional.ofNullable(optionAnswer.getValue()).orElse(StrUtil.EMPTY)).collect(Collectors.toList());

                answerDataBO.setAnswer(CollectionUtil.join(valueList,"、"));
            }

        }else {
            //处理
            Map<String, Option> optionMap = options.stream().collect(Collectors.toMap(Option::getId, Function.identity()));
            if (Objects.equals(question.getType(), CHECKBOX) || Objects.equals(question.getType(), RADIO)) {
                setAnswerData(answerDataBO, userAnswerList, optionMap);
            }
        }
        return answerDataBO;
    }

    /**
     * 设置单选、多选、单选+输入框、多选+输入框
     * @param answerDataBO  处理后的答案数据
     * @param userAnswerList 用户答案
     * @param optionMap 选项集合
     */
    private void setAnswerData(ExcelStudentDataBO.AnswerDataBO answerDataBO, List<UserAnswer> userAnswerList, Map<String, Option> optionMap) {
        Map<String,OptionAnswer> optionAnswerMap = userAnswerList.stream().flatMap(answer->{
            List<OptionAnswer> answerList = JSONObject.parseArray(JSONObject.toJSONString(answer.getAnswer()), OptionAnswer.class);
            return answerList.stream();
        }).collect(Collectors.toMap(OptionAnswer::getOptionId,Function.identity()));

        List<OptionAnswer> optionAnswerList = userAnswerList.stream().flatMap(answer->{
            List<OptionAnswer> answerList = JSONObject.parseArray(JSONObject.toJSONString(answer.getAnswer()), OptionAnswer.class);
            return answerList.stream();
        }).collect(Collectors.toList());
        List<String> answerList = Lists.newArrayList();
        for (OptionAnswer optionAnswer : optionAnswerList) {
            Option questionOption = optionMap.get(optionAnswer.getOptionId());
            if (Objects.isNull(questionOption)){
                continue;
            }
            JSONObject option = questionOption.getOption();
            if (Objects.nonNull(option) && option.size() > 0 ){
                // checkbox/radio 和input组合
                String answer = questionOption.getText();
                for (Map.Entry<String, Object> entry : option.entrySet()) {
                    JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(entry.getValue()), JSONObject.class);
                    OptionAnswer input = optionAnswerMap.get(json.getString("id"));
                    if (Objects.isNull(input)){
                        continue;
                    }
                    answer = answer.replace(String.format(PLACEHOLDER, entry.getKey()), Optional.ofNullable(input.getValue()).orElse(StrUtil.EMPTY));
                }
                answerList.add(answer);
            }else {
                //checkbox/radio
                answerList.add(questionOption.getText());
            }
        }
        answerDataBO.setAnswer(CollectionUtil.join(answerList," "));
    }

    /**
     * 获取导出Excel的数据
     * @param userQuestionRecordList 用户问卷记录集合
     * @param questionnaireIds 问卷ID集合
     * @param questionnaireId 隐藏问题问卷ID
     */
    public List<List<String>> getData(List<UserQuestionRecord> userQuestionRecordList,
                         List<Integer> questionnaireIds,Integer questionnaireId){
        List<ExcelStudentDataBO> excelStudentDataBOList = Lists.newArrayList();
        List<HideQuestionDataBO> hideQuestionDataBOList = questionnaireFacade.getHideQuestionnaireQuestion(questionnaireId);
        dataProcess(userQuestionRecordList,hideQuestionDataBOList, excelStudentDataBOList);
        List<Integer> questionIds = questionnaireFacade.getQuestionIdSort(questionnaireIds);
        return excelStudentDataBOList.stream().map(excelStudentDataBO -> {
            Map<Integer, String> answerDataMap = excelStudentDataBO.getAnswerDataMap();
            return questionIds.stream().map(answerDataMap::get).collect(Collectors.toList());
        }).collect(Collectors.toList());
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
     * 获取学生类型的Excel数据
     * @param mainBodyType 主问卷类型
     * @param baseInfoType 基础信息问卷类型
     * @param gradeTypeList 学龄集合
     * @param exportCondition 导出条件
     */
    public GenerateExcelDataBO generateStudentTypeExcelData(QuestionnaireTypeEnum mainBodyType, QuestionnaireTypeEnum baseInfoType,
                                                            List<Integer> gradeTypeList, ExportCondition exportCondition){
        //根据问卷类型获取问卷集合
        List<Questionnaire> questionnaireList = questionnaireFacade.getLatestQuestionnaire(mainBodyType);
        if (CollectionUtil.isEmpty(questionnaireList)){
            log.warn("暂无此问卷类型：{}",mainBodyType.getDesc());
            return null;
        }

        //获取用户问卷记录
        List<UserQuestionRecord> userQuestionRecordList = getQuestionnaireRecordList(exportCondition, questionnaireFacade.getQuestionnaireTypeList(mainBodyType), gradeTypeList);
        if (CollectionUtil.isEmpty(userQuestionRecordList)){
            log.info("暂无数据：notificationId:{}、planId:{}、taskId:{},问卷类型：{}",exportCondition.getNotificationId(),exportCondition.getPlanId(),exportCondition.getTaskId(),mainBodyType.getDesc());
            return null;
        }

        //获取学生类型问卷的 基础信息部分问卷ID
        Integer questionnaireId = questionnaireList.stream()
                .filter(questionnaire -> Objects.equals(questionnaire.getType(), baseInfoType.getType()))
                .findFirst().map(Questionnaire::getId).orElse(null);

        List<Integer> latestQuestionnaireIds =  questionnaireList.stream().sorted(Comparator.comparing(Questionnaire::getType).reversed()).map(Questionnaire::getId).collect(Collectors.toList());
        Map<Integer, List<UserQuestionRecord>> schoolRecordMap = userQuestionRecordList.stream()
                .filter(userQuestionRecord -> latestQuestionnaireIds.contains(userQuestionRecord.getQuestionnaireId()))
                .sorted(Comparator.comparing(UserQuestionRecord::getId))
                .collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));


        return getHeadAndData(latestQuestionnaireIds,questionnaireId,schoolRecordMap);

    }

    /**
     * 获取excel头信息和数据
     * @param latestQuestionnaireIds 最新问卷ID集合
     * @param questionnaireId 问卷基础部分对应的问卷ID
     * @param schoolRecordMap 学校对应用户记录集合
     */
    public GenerateExcelDataBO getHeadAndData(List<Integer> latestQuestionnaireIds,Integer questionnaireId,Map<Integer, List<UserQuestionRecord>> schoolRecordMap){
        GenerateExcelDataBO generateExcelDataBO = new GenerateExcelDataBO();
        List<List<String>> head = questionnaireFacade.getHead(latestQuestionnaireIds);

        Map<Integer,List<List<String>>> dataMap= Maps.newHashMap();
        for (Map.Entry<Integer, List<UserQuestionRecord>> entry : schoolRecordMap.entrySet()) {
            dataMap.put(entry.getKey(), getData(entry.getValue(), latestQuestionnaireIds,questionnaireId));
        }

        generateExcelDataBO.setHead(head);
        generateExcelDataBO.setDataMap(dataMap);

        //根据问卷ID集合，移除计算分值问题ID
        questionnaireFacade.removeScoreQuestionId(latestQuestionnaireIds);
        return generateExcelDataBO;
    }

    /**
     * 获取文件夹地址集合
     * @param exportCondition 导出条件
     * @param fileName 文件地址路径
     */
    public List<String> getFolder(ExportCondition exportCondition,String fileName) {
        Optional<ExportType> exportTypeService = questionnaireExcelFactory.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            return exportType.getFolder(exportCondition,fileName);
        }
        throw new BusinessException(String.format(ExportQuestionnaireService.ERROR_MSG,exportCondition.getExportType()));
    }
}

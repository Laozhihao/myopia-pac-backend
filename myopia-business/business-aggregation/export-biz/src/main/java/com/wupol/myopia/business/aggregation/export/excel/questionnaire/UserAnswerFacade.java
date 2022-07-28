package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireStatusEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.domain.dos.*;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户问卷答案
 *
 * @author hang.yuan 2022/7/21 19:50
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserAnswerFacade {

    private final UserQuestionRecordService userQuestionRecordService;
    private final UserAnswerService userAnswerService;
    private final QuestionService questionService;
    private final SchoolService schoolService;
    private final ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    private final QuestionnaireFacade questionnaireFacade;
    private final QuestionnaireExcelFactory questionnaireExcelFactory;

    private static final String  PLACEHOLDER = "-{%s}";
    private static final String  ID = "id";
    private static final String  RADIO = "radio";
    private static final String  INPUT = "input";
    private static final String  CHECKBOX = "checkbox";


    private List<Integer> getConditionValue(ExportCondition exportCondition){
        Optional<ExportType> exportTypeService = questionnaireExcelFactory.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()){
            ExportType exportType = exportTypeService.get();
            return exportType.getConditionValue(exportCondition);
        }
        return defaultValue(null,null,null);
    }

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
            List<UserQuestionRecord> collect = userQuestionRecordList.stream()
                    .filter(userQuestionRecord -> !Objects.equals(userQuestionRecord.getStatus(), QuestionnaireStatusEnum.NOT_START.getCode()))
                    .filter(userQuestionRecord -> questionnaireTypeList.contains(userQuestionRecord.getQuestionnaireType()))
                    .collect(Collectors.toList());

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
                if (Objects.equals(userQuestionRecord.getQuestionnaireType(), QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType())){
                    //处理隐藏数据（学生和学校数据）
                    List<ExcelStudentDataBO.AnswerDataBO> answerDataBOList = hideQuestionDataProcess(userQuestionRecord.getUserId(),fillDate,hideQuestionDataBOList);
                    excelStudentDataBO.setDataList(answerDataBOList);
                }
                List<UserAnswer> userAnswers = userAnswerMap.get(userQuestionRecord.getId());
                if (CollectionUtil.isNotEmpty(userAnswers)){
                    Map<Integer, List<UserAnswer>> questionUserAnswerMap  = userAnswers.stream().collect(Collectors.groupingBy(UserAnswer::getQuestionId));
                    List<ExcelStudentDataBO.AnswerDataBO> collect =Lists.newArrayList();
                    questionUserAnswerMap.forEach((questionId,list)-> collect.add(getAnswerData(list,questionMap)));
                    if (Objects.isNull(excelStudentDataBO.getDataList())) {
                        excelStudentDataBO.setDataList(collect);
                    }else {
                        excelStudentDataBO.getDataList().addAll(collect);
                    }
                }
            }
            excelStudentDataBOList.add(excelStudentDataBO);
        });

    }

    private List<ExcelStudentDataBO.AnswerDataBO> hideQuestionDataProcess(Integer planStudentId ,Date fillDate,
                                                                          List<HideQuestionDataBO> hideQuestionDataBOList){
        List<ExcelStudentDataBO.AnswerDataBO> answerDataBOList =Lists.newArrayList();

        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = screeningPlanSchoolStudentService.getById(planStudentId);
        School school = schoolService.getById(screeningPlanSchoolStudent.getSchoolId());

        for (int i = 0; i < hideQuestionDataBOList.size(); i++) {
            ExcelStudentDataBO.AnswerDataBO answerDataBO = new ExcelStudentDataBO.AnswerDataBO();
            answerDataBO.setQuestionId(hideQuestionDataBOList.get(i).getQuestionId());
            String districtAreaCode = school.getDistrictAreaCode().toString();
            switch (i){
                case 0:
                    answerDataBO.setAnswer(screeningPlanSchoolStudent.getCommonDiseaseId());
                    answerDataBOList.add(answerDataBO);
                    break;
                case 1:
                    answerDataBO.setAnswer(districtAreaCode.substring(0,2));
                    answerDataBOList.add(answerDataBO);
                    break;
                case 2:
                    answerDataBO.setAnswer(districtAreaCode.substring(2,4));
                    answerDataBOList.add(answerDataBO);
                    break;
                case 3:
                    answerDataBO.setAnswer(Optional.ofNullable(school.getAreaType()).map(Object::toString).orElse(StrUtil.EMPTY));
                    answerDataBOList.add(answerDataBO);
                    break;
                case 4:
                    answerDataBO.setAnswer(districtAreaCode.substring(4,6));
                    answerDataBOList.add(answerDataBO);
                    break;
                case 5:
                    answerDataBO.setAnswer(Optional.ofNullable(school.getMonitorType()).map(Object::toString).orElse(StrUtil.EMPTY));
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
                            answer = answer.replace(String.format(PLACEHOLDER, entry.getKey()), optionAnswer.getValue());
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
                }).map(OptionAnswer::getValue).collect(Collectors.toList());

                answerDataBO.setAnswer(CollectionUtil.join(valueList,"、"));
            }

        }else {
            Map<String, Option> optionMap = options.stream().collect(Collectors.toMap(Option::getId, Function.identity()));
            if (Objects.equals(question.getType(), CHECKBOX)) {
                setAnswerData(answerDataBO, userAnswer, optionMap);
            }
            else if (Objects.equals(question.getType(), RADIO)) {
                setAnswerData(answerDataBO,userAnswer,optionMap);
            }
        }
        return answerDataBO;
    }

    private void setAnswerData(ExcelStudentDataBO.AnswerDataBO answerDataBO, UserAnswer userAnswer, Map<String, Option> optionMap) {
        List<OptionAnswer> optionAnswerList = JSONObject.parseArray(JSONObject.toJSONString(userAnswer.getAnswer()), OptionAnswer.class);
        List<String> answerList = Lists.newArrayList();
        for (OptionAnswer optionAnswer : optionAnswerList) {
            Option questionOption = optionMap.get(optionAnswer.getOptionId());
            JSONObject option = questionOption.getOption();
            if (Objects.nonNull(option) && option.size() > 0 ){
                // checkbox/radio 和input组合
                String answer = questionOption.getText();
                for (Map.Entry<String, Object> entry : option.entrySet()) {
                    answer = answer.replace(String.format(PLACEHOLDER, entry.getKey()), Optional.ofNullable(optionAnswer.getValue()).orElse(StrUtil.EMPTY));
                }
                answerList.add(answer);
            }else {
                //checkbox/radio
                answerList.add(questionOption.getText());
            }
        }
        answerDataBO.setAnswer(CollectionUtil.join(answerList," "));
    }


    public List getData(List<UserQuestionRecord> userQuestionRecordList,
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

}

package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.aggregation.export.excel.domain.*;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireStatusEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.dos.*;
import com.wupol.myopia.business.core.questionnaire.domain.model.*;
import com.wupol.myopia.business.core.questionnaire.facade.QuestionnaireFacade;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.questionnaire.util.EpiDataUtil;
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
    private final ResourceFileService resourceFileService;

    private static final String FILE_NAME = "%s的%s的rec文件";


    /**
     * 获取条件值（通知ID,任务ID,计划ID）
     *
     * @param exportCondition 导出条件
     */
    private List<Integer> getConditionValue(ExportCondition exportCondition) {
        Optional<ExportType> exportTypeService = questionnaireExcelFactory.getExportTypeService(exportCondition.getExportType());
        if (exportTypeService.isPresent()) {
            ExportType exportType = exportTypeService.get();
            return exportType.getConditionValue(exportCondition);
        }
        return UserAnswerBuilder.defaultValue(null, null, null);
    }


    /**
     * 获取问卷记录数（有数据的问卷 状态进行中或者已完成）
     *
     * @param exportCondition       导出条件
     * @param questionnaireTypeList 问卷类型集合
     */
    public List<UserQuestionRecord> getQuestionnaireRecordList(ExportCondition exportCondition, List<Integer> questionnaireTypeList, List<Integer> gradeTypeList, List<Integer> conditionValue) {
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getListByNoticeIdOrTaskIdOrPlanId(conditionValue.get(0), conditionValue.get(1), conditionValue.get(2), QuestionnaireStatusEnum.FINISH.getCode());
        if (CollectionUtils.isEmpty(userQuestionRecordList)) {
            return Lists.newArrayList();
        }

        userQuestionRecordList = filterQuestionnaireTypeOrSchool(questionnaireTypeList, exportCondition.getSchoolId(), userQuestionRecordList);
        if (CollectionUtils.isEmpty(userQuestionRecordList)) {
            return Lists.newArrayList();
        }

        return filterStudent(exportCondition.getDistrictId(), userQuestionRecordList, gradeTypeList);
    }

    /**
     * 过滤问卷类型 或这学校
     *
     * @param questionnaireTypeList  问卷类型集合
     * @param schoolId               学校ID
     * @param userQuestionRecordList 用户问卷记录集合
     */
    private List<UserQuestionRecord> filterQuestionnaireTypeOrSchool(List<Integer> questionnaireTypeList, Integer schoolId, List<UserQuestionRecord> userQuestionRecordList) {

        Stream<UserQuestionRecord> userQuestionRecordStream = userQuestionRecordList.stream()
                .filter(userQuestionRecord -> questionnaireTypeList.contains(userQuestionRecord.getQuestionnaireType()));

        if (Objects.isNull(schoolId)) {
            return userQuestionRecordStream.collect(Collectors.toList());
        }
        return userQuestionRecordStream
                .filter(userQuestionRecord -> Objects.equals(userQuestionRecord.getSchoolId(), schoolId))
                .collect(Collectors.toList());
    }

    /**
     * 年级类型过滤计划学生
     *
     * @param districtId             地区ID
     * @param userQuestionRecordList 用户问卷记录集合
     * @param gradeTypeList          年级类型集合
     */
    private List<UserQuestionRecord> filterStudent(Integer districtId, List<UserQuestionRecord> userQuestionRecordList, List<Integer> gradeTypeList) {
        if (CollUtil.isEmpty(userQuestionRecordList)) {
            return userQuestionRecordList;
        }

        Set<Integer> planStudentIds = userQuestionRecordList.stream().map(UserQuestionRecord::getUserId).collect(Collectors.toSet());
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(planStudentIds));
        //年级过滤学生记录
        planSchoolStudentList = planSchoolStudentList.stream()
                .filter(planSchoolStudent -> gradeTypeList.contains(planSchoolStudent.getGradeType()))
                .collect(Collectors.toList());

        if (Objects.nonNull(districtId)) {
            //根据区域过滤
            return filterDistrict(districtId, userQuestionRecordList, planSchoolStudentList);
        }

        List<Integer> planStudentIdList = planSchoolStudentList.stream()
                .map(ScreeningPlanSchoolStudent::getId)
                .collect(Collectors.toList());

        //根据计划学生过滤用户问卷记录
        return userQuestionRecordList.stream()
                .filter(userQuestionRecord -> planStudentIdList.contains(userQuestionRecord.getUserId()))
                .collect(Collectors.toList());

    }

    /**
     * 过滤区域
     *
     * @param districtId             区域ID
     * @param userQuestionRecordList 用户问卷记录集合
     * @param planSchoolStudentList  计划学生集合
     */
    private List<UserQuestionRecord> filterDistrict(Integer districtId, List<UserQuestionRecord> userQuestionRecordList, List<ScreeningPlanSchoolStudent> planSchoolStudentList) {
        if (Objects.isNull(districtId) || CollUtil.isEmpty(planSchoolStudentList)) {
            return userQuestionRecordList;
        }

        List<Integer> districtIdList = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        if (!districtIdList.contains(districtId)) {
            districtIdList.add(districtId);
        }

        List<Integer> planStudentIdList = planSchoolStudentList.stream()
                .filter(planSchoolStudent -> districtIdList.contains(planSchoolStudent.getSchoolDistrictId()))
                .map(ScreeningPlanSchoolStudent::getId)
                .collect(Collectors.toList());

        return userQuestionRecordList.stream()
                .filter(userQuestionRecord -> planStudentIdList.contains(userQuestionRecord.getUserId()))
                .collect(Collectors.toList());
    }


    /**
     * 获取导出Excel的数据
     *
     * @param userQuestionnaireAnswerBOList 用户问卷记录集合
     * @param dataBuildList                 问题Rec数据结构集合
     * @param qesFieldList                  有序问卷字段
     */
    public Map<Integer,List<QuestionnaireRecDataBO>> getRecData(List<UserQuestionnaireAnswerBO> userQuestionnaireAnswerBOList,
                                                   List<QuestionnaireQuestionRecDataBO> dataBuildList,
                                                   List<String> qesFieldList) {

        Map<Integer,List<QuestionnaireRecDataBO>> dataMap = Maps.newHashMap();

        for (UserQuestionnaireAnswerBO userQuestionnaireAnswerBO : userQuestionnaireAnswerBOList) {

            List<QuestionnaireRecDataBO> dataList = Lists.newArrayList();
            Map<Integer, Map<String, OptionAnswer>> questionAnswerMap = userQuestionnaireAnswerBO.getAnswerMap();

            for (QuestionnaireQuestionRecDataBO questionnaireQuestionRecDataBO : dataBuildList) {
                processAnswerData(dataList, questionAnswerMap, questionnaireQuestionRecDataBO);
            }

            Map<String, QuestionnaireRecDataBO> studentDataMap = dataList.stream().collect(Collectors.toMap(QuestionnaireRecDataBO::getQesField, Function.identity()));
            List<QuestionnaireRecDataBO> collect = qesFieldList.stream().map(studentDataMap::get).collect(Collectors.toList());

            dataMap.put(userQuestionnaireAnswerBO.getStudentId(),collect);
        }
        return dataMap;
    }

    /**
     * 处理答案数据
     *
     * @param dataList                       结果集合
     * @param questionAnswerMap              问题集合
     * @param questionnaireQuestionRecDataBO 问卷问题Rec数据信息
     */
    private void processAnswerData(List<QuestionnaireRecDataBO> dataList, Map<Integer, Map<String, OptionAnswer>> questionAnswerMap, QuestionnaireQuestionRecDataBO questionnaireQuestionRecDataBO) {
        Question question = questionnaireQuestionRecDataBO.getQuestion();
        Map<String, OptionAnswer> answerMap = questionAnswerMap.getOrDefault(question.getId(), Maps.newHashMap());
        List<QuestionnaireRecDataBO> recDataList = questionnaireQuestionRecDataBO.getQuestionnaireRecDataBOList();
        if (Objects.equals(question.getType(), QuestionnaireConstant.INPUT)) {
            getInputData(dataList, answerMap, recDataList);
        }
        if (Objects.equals(question.getType(), QuestionnaireConstant.RADIO)) {
            recDataList.forEach(questionnaireRecDataBO -> setRadioOrCheckbox(dataList, answerMap, questionnaireRecDataBO, Boolean.FALSE));
        }
        if (Objects.equals(question.getType(), QuestionnaireConstant.CHECKBOX)) {
            recDataList.forEach(questionnaireRecDataBO -> setRadioOrCheckbox(dataList, answerMap, questionnaireRecDataBO, Boolean.TRUE));
        }
    }

    /**
     * 设置单元或者多选类型的数据
     *
     * @param dataList               结果集合
     * @param answerMap              问题集合
     * @param questionnaireRecDataBO 问卷Rec数据信息
     * @param isCheckbox             是否是多选
     */
    private void setRadioOrCheckbox(List<QuestionnaireRecDataBO> dataList,
                                    Map<String, OptionAnswer> answerMap,
                                    QuestionnaireRecDataBO questionnaireRecDataBO,
                                    Boolean isCheckbox) {
        OptionAnswer optionAnswer = answerMap.get(questionnaireRecDataBO.getOptionId());
        if (Objects.isNull(optionAnswer)) {
            if (Objects.equals(Boolean.TRUE, isCheckbox)) {
                getRadioOrCheckboxData(dataList, answerMap, questionnaireRecDataBO, "2");
            }
            return;
        }
        getRadioOrCheckboxData(dataList, answerMap, questionnaireRecDataBO, "1");
    }

    /**
     * 获取Input类型数据
     *
     * @param dataList    结果集合
     * @param answerMap   问题集合
     * @param recDataList 问卷Rec数据信息集合
     */
    private void getInputData(List<QuestionnaireRecDataBO> dataList, Map<String, OptionAnswer> answerMap, List<QuestionnaireRecDataBO> recDataList) {
        for (QuestionnaireRecDataBO questionnaireRecDataBO : recDataList) {
            String answer = Optional.ofNullable(answerMap.get(questionnaireRecDataBO.getOptionId())).map(OptionAnswer::getValue).orElse(StrUtil.EMPTY);
            if (Objects.equals(questionnaireRecDataBO.getDataType(), QuestionnaireConstant.NUMBER)) {
                questionnaireRecDataBO.setRecAnswer(UserAnswerBuilder.numberFormat(answer));
            }
            if (Objects.equals(questionnaireRecDataBO.getDataType(), QuestionnaireConstant.TEXT)) {
                questionnaireRecDataBO.setRecAnswer(UserAnswerBuilder.textFormat(answer));
            }
            dataList.add(questionnaireRecDataBO);
        }
    }

    /**
     * 获取单元或者多选类型的数据
     *
     * @param dataList               结果集合
     * @param answerMap              问题集合
     * @param questionnaireRecDataBO 问卷Rec数据信息
     * @param recAnswer              rec答案
     */
    private void getRadioOrCheckboxData(List<QuestionnaireRecDataBO> dataList,
                                        Map<String, OptionAnswer> answerMap,
                                        QuestionnaireRecDataBO questionnaireRecDataBO,
                                        String recAnswer) {
        questionnaireRecDataBO.setRecAnswer(recAnswer);
        dataList.add(questionnaireRecDataBO);
        //单选或者多选Input
        getRadioOrCheckboxInputData(dataList, answerMap, questionnaireRecDataBO);
    }

    /**
     * 获取单元或者多选Input类型的数据
     *
     * @param dataList               结果集合
     * @param answerMap              问题集合
     * @param questionnaireRecDataBO 问卷Rec数据信息
     */
    private void getRadioOrCheckboxInputData(List<QuestionnaireRecDataBO> dataList,
                                             Map<String, OptionAnswer> answerMap,
                                             QuestionnaireRecDataBO questionnaireRecDataBO) {
        List<QuestionnaireRecDataBO> questionnaireRecDataBOList = questionnaireRecDataBO.getQuestionnaireRecDataBOList();
        if (CollUtil.isEmpty(questionnaireRecDataBOList)) {
            return;
        }
        getInputData(dataList, answerMap, questionnaireRecDataBOList);
    }

    private List<UserQuestionnaireAnswerBO> getUserQuestionnaireAnswerBOList(List<UserQuestionRecord> userQuestionRecordList, List<HideQuestionRecDataBO> hideQuestionDataBOList) {
        List<Integer> recordIds = userQuestionRecordList.stream().map(UserQuestionRecord::getId).collect(Collectors.toList());
        List<UserAnswer> userAnswerList = userAnswerService.getListByRecordIds(recordIds);

        Set<Integer> planStudentIds = userQuestionRecordList.stream().map(UserQuestionRecord::getUserId).collect(Collectors.toSet());
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(planStudentIds));

        UserQuestionnaireAnswerInfoBuilder build = UserQuestionnaireAnswerInfoBuilder.builder()
                .userQuestionRecordList(userQuestionRecordList)
                .userAnswerMap(userAnswerList.stream().collect(Collectors.groupingBy(UserAnswer::getRecordId)))
                .hideQuestionDataBOList(hideQuestionDataBOList)
                .planSchoolStudentMap(planSchoolStudentList.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getStudentId, Function.identity())))
                .build();

        return build.dataBuild();
    }


    /**
     * 获取rec文件名称
     *
     * @param schoolId          学校ID
     * @param questionnaireType 问卷类型
     */
    public String getRecFileName(Integer schoolId, Integer questionnaireType) {
        School school = schoolService.getById(schoolId);
        QuestionnaireTypeEnum questionnaireTypeEnum = QuestionnaireTypeEnum.getQuestionnaireType(questionnaireType);
        return String.format(FILE_NAME, school.getName(), questionnaireTypeEnum.getDesc());
    }


    /**
     * 获取rec数据
     *
     * @param generateDataCondition 生成数据条件
     */
    public List<GenerateRecDataBO>  generateRecData(GenerateDataCondition generateDataCondition) {
        TwoTuple<List<Questionnaire>, List<UserQuestionRecord>> tuple = getBaseData(generateDataCondition);
        if (Objects.isNull(tuple)){
            return Lists.newArrayList();
        }

        //获取学生类型问卷的 基础信息部分问卷ID
        Integer questionnaireId = tuple.getFirst().stream()
                .filter(questionnaire -> Objects.equals(questionnaire.getType(), generateDataCondition.getBaseInfoType().getType()))
                .findFirst().map(Questionnaire::getId).orElse(null);


        List<Integer> latestQuestionnaireIds = tuple.getFirst().stream().map(Questionnaire::getId).collect(Collectors.toList());

        //学校对应用户问卷记录
        Map<Integer, List<UserQuestionRecord>> schoolRecordMap = tuple.getSecond().stream()
                .filter(userQuestionRecord -> latestQuestionnaireIds.contains(userQuestionRecord.getQuestionnaireId()))
                .sorted(Comparator.comparing(UserQuestionRecord::getId))
                .collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));

        List<QuestionnaireQuestionRecDataBO> dataBuildList = questionnaireFacade.getDataBuildList(latestQuestionnaireIds);

        List<HideQuestionRecDataBO> hideQuestionDataBOList = questionnaireFacade.getHideQuestionnaireQuestionRec(questionnaireId);

        List<QesFieldMapping> qesFieldMappingList = questionnaireFacade.getQesFieldMappingList(latestQuestionnaireIds);

        List<String> qesFieldList = qesFieldMappingList.stream()
                .map(qesFieldMapping -> UserAnswerBuilder.getQesFieldStr(qesFieldMapping.getQesField()))
                .collect(Collectors.toList());


        Map<Integer, Map<Integer,List<QuestionnaireRecDataBO>>> schoolAnswerMap = Maps.newHashMap();
        schoolRecordMap.forEach((schoolId, recordList) -> {
            List<UserQuestionnaireAnswerBO> userQuestionnaireAnswerBOList = getUserQuestionnaireAnswerBOList(recordList, hideQuestionDataBOList);
            schoolAnswerMap.put(schoolId, getRecData(userQuestionnaireAnswerBOList, dataBuildList, qesFieldList));
        });

        Integer qesFileId = questionnaireFacade.getQesFileId(qesFieldMappingList.get(0).getQesId());
        String qesUrl = resourceFileService.getResourcePath(qesFileId);

        return schoolAnswerMap.entrySet().stream()
                .map(entry -> buildGenerateRecDataBO(qesFieldList, qesUrl, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

    }

    private TwoTuple<List<Questionnaire>,List<UserQuestionRecord>> getBaseData(GenerateDataCondition generateDataCondition){
        QuestionnaireTypeEnum mainBodyType = generateDataCondition.getMainBodyType();
        ExportCondition exportCondition = generateDataCondition.getExportCondition();
        //根据问卷类型获取问卷集合
        List<Questionnaire> questionnaireList = questionnaireFacade.getLatestQuestionnaire(mainBodyType, QuestionnaireConstant.REC_FILE);
        if (CollUtil.isEmpty(questionnaireList)) {
            log.warn("暂无此问卷类型：{}", mainBodyType.getDesc());
            return null;
        }

        //获取用户问卷记录
        List<UserQuestionRecord> userQuestionRecordList = getQuestionnaireRecordList(exportCondition,
                questionnaireFacade.getQuestionnaireTypeList(mainBodyType, QuestionnaireConstant.REC_FILE),
                generateDataCondition.getGradeTypeList(),
                getConditionValue(exportCondition));

        if (CollUtil.isEmpty(userQuestionRecordList)) {
            log.info("暂无数据：notificationId:{}、planId:{}、taskId:{},问卷类型：{}", exportCondition.getNotificationId(), exportCondition.getPlanId(), exportCondition.getTaskId(), mainBodyType.getDesc());
            return null;
        }

        return TwoTuple.of(questionnaireList,userQuestionRecordList);
    }

    private GenerateRecDataBO buildGenerateRecDataBO(List<String> qesFieldList, String qesUrl, Integer schoolId, Map<Integer, List<QuestionnaireRecDataBO>> studentAnswersMap) {
        List<List<String>> dataList = new ArrayList<>();
        studentAnswersMap.forEach((studentId,answerList)-> dataList.add(answerList.stream().map(QuestionnaireRecDataBO::getRecAnswer).collect(Collectors.toList())));
        String txtPath = EpiDataUtil.createTxtPath(qesFieldList, dataList);
        System.out.println(txtPath);
        List<String> dataTxt = EpiDataUtil.mergeDataTxt(qesFieldList, dataList);
        return new GenerateRecDataBO(schoolId,qesUrl,dataTxt);
    }

}

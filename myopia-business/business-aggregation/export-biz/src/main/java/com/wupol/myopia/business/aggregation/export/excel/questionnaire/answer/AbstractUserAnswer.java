package com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.aggregation.export.excel.domain.*;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.QuestionnaireFactory;
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
import com.wupol.myopia.business.core.questionnaire.service.UserAnswerService;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.questionnaire.util.AnswerUtil;
import com.wupol.myopia.business.core.questionnaire.util.EpiDataUtil;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.rec.client.RecServiceClient;
import com.wupol.myopia.rec.domain.RecExportDTO;
import com.wupol.myopia.rec.domain.RecExportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 抽象用户答案
 *
 * @author hang.yuan 2022/8/25 10:23
 */
@Slf4j
@Service
public abstract class AbstractUserAnswer implements Answer {


    @Autowired
    private QuestionnaireFactory questionnaireFactory;
    @Autowired
    private UserQuestionRecordService userQuestionRecordService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private RecServiceClient recServiceClient;
    @Autowired
    private ThreadPoolTaskExecutor asyncServiceExecutor;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private QuestionnaireFacade questionnaireFacade;
    @Autowired
    private UserAnswerService userAnswerService;
    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;


    private static final String FILE_NAME = "%s的%s的rec文件";

    /**
     * 获取条件值（通知ID,任务ID,计划ID）
     *
     * @param exportCondition 导出条件
     */
    private List<Integer> getConditionValue(ExportCondition exportCondition) {
        ExportType exportTypeService = questionnaireFactory.getExportTypeService(exportCondition.getExportType());
        return exportTypeService.getConditionValue(exportCondition);
    }

    /**
     * 获取问卷记录数（有数据的问卷 状态进行中或者已完成）
     *
     * @param questionnaireTypeList 问卷类型集合
     * @param conditionValue        查询条件值集合
     * @param userType              用户类型
     */
    public List<UserQuestionRecord> getQuestionnaireRecordList(List<Integer> questionnaireTypeList,
                                                               List<Integer> conditionValue,
                                                               Integer userType) {
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordService.getListByNoticeIdOrTaskIdOrPlanId(conditionValue.get(0), conditionValue.get(1), conditionValue.get(2), QuestionnaireStatusEnum.FINISH.getCode());
        if (CollectionUtils.isEmpty(userQuestionRecordList)) {
            return Lists.newArrayList();
        }
        //过滤用户类型
        return userQuestionRecordList.stream()
                .filter(userQuestionRecord -> Objects.equals(userQuestionRecord.getUserType(), userType))
                .filter(userQuestionRecord -> questionnaireTypeList.contains(userQuestionRecord.getQuestionnaireType()))
                .collect(Collectors.toList());
    }

    /**
     * 获取rec文件名称
     *
     * @param schoolId          学校ID
     * @param questionnaireType 问卷类型
     */
    @Override
    public String getRecFileName(Integer schoolId, Integer questionnaireType) {
        School school = schoolService.getById(schoolId);
        QuestionnaireTypeEnum questionnaireTypeEnum = QuestionnaireTypeEnum.getQuestionnaireType(questionnaireType);
        return String.format(FILE_NAME, school.getName(), questionnaireTypeEnum.getDesc());
    }

    /**
     * 构建导出条件
     *
     * @param generateRecDataBO 生成rec数据
     * @param recFileName 问卷类型
     */
    private RecExportDTO buildRecExportDTO(GenerateRecDataBO generateRecDataBO, String recFileName) {
        RecExportDTO recExportDTO = new RecExportDTO();
        recExportDTO.setQesUrl(generateRecDataBO.getQesUrl());
        recExportDTO.setDataList(generateRecDataBO.getDataList());
        recExportDTO.setRecName(recFileName);
        return recExportDTO;
    }

    /**
     * 调rec导出工具
     *
     * @param fileName          文件夹
     * @param generateRecDataBO 导出条件
     * @param generateRecDataBO 导出条件
     */
    @Override
    public void exportRecFile(String fileName, GenerateRecDataBO generateRecDataBO, String recFileName) {
        RecExportDTO recExportDTO = buildRecExportDTO(generateRecDataBO, recFileName);

        log.info("请求参数：{}", JSON.toJSONString(recExportDTO));
        CompletableFuture<RecExportVO> future = CompletableFuture.supplyAsync(() -> recServiceClient.export(recExportDTO), asyncServiceExecutor);
        try {
            RecExportVO recExportVO = future.get();
            String recPath = EpiDataUtil.getRecPath(recExportVO.getRecUrl(), fileName, recExportVO.getRecName());
            recFileMove(recPath,fileName,recExportVO.getRecName());
            log.info("生成rec文件成功 recName={}", recExportVO.getRecName());
        } catch (InterruptedException e) {
            log.warn("Interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("获取rec导出结果失败");
        }
    }

    private static void recFileMove(String recZip,String epiDataPath, String recFolderName){
        ZipUtil.unzip(recZip,epiDataPath);
        recFolderName = Paths.get(epiDataPath, recFolderName).toString();
        File[] files = FileUtil.newFile(recFolderName).listFiles();
        if (ArrayUtil.isEmpty(files)){
            return;
        }
        for (File file : files) {
            FileUtil.move(file,FileUtil.newFile(epiDataPath),true);
        }
        FileUtil.del(recZip);
        FileUtil.del(recFolderName);
    }

    /**
     * 过滤区域
     *
     * @param districtId 区域ID
     */
    protected List<Integer> filterDistrict(Integer districtId) {
        if (Objects.isNull(districtId)) {
            return null;
        }
        List<Integer> districtIdList = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        if (!districtIdList.contains(districtId)) {
            districtIdList.add(districtId);
        }
        return districtIdList;

    }

    /**
     * 获取导出REC的数据
     *
     * @param userQuestionnaireAnswerBOList 用户问卷记录集合
     * @param dataBuildList                 问题Rec数据结构集合
     * @param qesFieldList                  有序问卷字段
     */
    public Map<String, List<QuestionnaireRecDataBO>> getRecData(List<UserQuestionnaireAnswerBO> userQuestionnaireAnswerBOList,
                                                                List<QuestionnaireQuestionRecDataBO> dataBuildList,
                                                                List<String> qesFieldList) {

        Map<String, List<QuestionnaireRecDataBO>> dataMap = Maps.newHashMap();

        //学校里的每个学生或者用户
        for (UserQuestionnaireAnswerBO userQuestionnaireAnswerBO : userQuestionnaireAnswerBOList) {

            List<QuestionnaireRecDataBO> dataList = Lists.newArrayList();
            Map<Integer, Map<String, OptionAnswer>> questionAnswerMap = userQuestionnaireAnswerBO.getAnswerMap();
            //每个学生或者用户完成数据
            for (QuestionnaireQuestionRecDataBO questionnaireQuestionRecDataBO : dataBuildList) {
                if (Objects.equals(Boolean.TRUE, questionnaireQuestionRecDataBO.getIsHidden())) {
                    //隐藏问题
                    hideQuestionDataProcess(dataList, questionnaireQuestionRecDataBO, userQuestionnaireAnswerBO);
                    continue;
                }
                processAnswerData(dataList, questionAnswerMap, questionnaireQuestionRecDataBO);
            }
            Map<String, QuestionnaireRecDataBO> studentDataMap = dataList.stream().collect(Collectors.toMap(questionnaireRecDataBO -> AnswerUtil.getQesFieldStr(questionnaireRecDataBO.getQesField()), Function.identity()));
            List<QuestionnaireRecDataBO> collect = qesFieldList.stream().map(studentDataMap::get).collect(Collectors.toList());
            dataMap.put(userQuestionnaireAnswerBO.getUserKey(), collect);
        }
        return dataMap;
    }


    /**
     * 隐藏问题处理
     *
     * @param dataList                       结果集合
     * @param questionnaireQuestionRecDataBO 问卷问题rec数据结构对象
     * @param userQuestionnaireAnswerBO      用户问卷答案对象
     */
    private void hideQuestionDataProcess(List<QuestionnaireRecDataBO> dataList,
                                         QuestionnaireQuestionRecDataBO questionnaireQuestionRecDataBO,
                                         UserQuestionnaireAnswerBO userQuestionnaireAnswerBO) {
        Question question = questionnaireQuestionRecDataBO.getQuestion();
        List<QuestionnaireRecDataBO> questionnaireRecDataBOList = questionnaireQuestionRecDataBO.getQuestionnaireRecDataBOList();
        Map<String, List<QuestionnaireRecDataBO>> questionnaireRecDataMap = questionnaireRecDataBOList.stream().collect(Collectors.groupingBy(QuestionnaireRecDataBO::getQesField));

        List<QesFieldDataBO> qesFieldDataBOList = userQuestionnaireAnswerBO.getQesFieldDataBOList();
        if (CollUtil.isEmpty(qesFieldDataBOList)){
            return;
        }
        Map<String, QesFieldDataBO> qesFieldDataBoMap = qesFieldDataBOList.stream().collect(Collectors.toMap(QesFieldDataBO::getQesField, Function.identity()));
        questionnaireRecDataMap.forEach((qesField, recDataList) -> {

            if (Objects.equals(question.getType(), QuestionnaireConstant.INPUT)) {
                getHideInputData(dataList, qesFieldDataBoMap, recDataList);
            }
            if (Objects.equals(question.getType(), QuestionnaireConstant.RADIO)) {
                setHideRadio(dataList, qesFieldDataBoMap, recDataList.get(0));
            }
        });
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
            getRadioData(dataList, answerMap, recDataList);
        }
        if (Objects.equals(question.getType(), QuestionnaireConstant.CHECKBOX)) {
            recDataList.forEach(questionnaireRecDataBO -> getCheckboxData(dataList, answerMap, questionnaireRecDataBO));
        }
    }

    /**
     * 获取隐藏Input类型数据
     *
     * @param dataList          结果集合
     * @param qesFieldDataBOMap qes字段数据集合
     * @param recDataList       问卷Rec数据信息集合
     */
    private void getHideInputData(List<QuestionnaireRecDataBO> dataList, Map<String, QesFieldDataBO> qesFieldDataBOMap, List<QuestionnaireRecDataBO> recDataList) {
        for (QuestionnaireRecDataBO questionnaireRecDataBO : recDataList) {
            String answer = Optional.ofNullable(qesFieldDataBOMap.get(questionnaireRecDataBO.getQesField())).map(qesFieldDataBO -> Optional.ofNullable(qesFieldDataBO.getRecAnswer()).orElse(StrUtil.EMPTY)).orElse(StrUtil.EMPTY);
            if (Objects.equals(questionnaireRecDataBO.getDataType(), QuestionnaireConstant.NUMBER)) {
                if (Objects.equals(questionnaireRecDataBO.getQesField(), "ID1") || Objects.equals(questionnaireRecDataBO.getQesField(), "ID2")) {
                    questionnaireRecDataBO.setRecAnswer(answer);
                } else {
                    questionnaireRecDataBO.setRecAnswer(AnswerUtil.numberFormat(answer, questionnaireRecDataBO.getRange()));
                }
            }
            if (Objects.equals(questionnaireRecDataBO.getDataType(), QuestionnaireConstant.TEXT)) {
                if (Objects.equals(questionnaireRecDataBO.getQesField(), "date")) {
                    questionnaireRecDataBO.setRecAnswer(answer);
                } else {
                    questionnaireRecDataBO.setRecAnswer(AnswerUtil.textFormat(answer));
                }
            }
            dataList.add(questionnaireRecDataBO);
        }
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
                questionnaireRecDataBO.setRecAnswer(AnswerUtil.numberFormat(answer, questionnaireRecDataBO.getRange()));
            }
            if (Objects.equals(questionnaireRecDataBO.getDataType(), QuestionnaireConstant.TEXT)) {
                questionnaireRecDataBO.setRecAnswer(AnswerUtil.textFormat(answer));
            }
            dataList.add(questionnaireRecDataBO);
        }
    }

    /**
     * 设置隐藏单选数据
     *
     * @param dataList               结果集合
     * @param qesFieldDataBoMap      qes字段数据集合
     * @param questionnaireRecDataBO 问卷Rec数据信息
     */
    private void setHideRadio(List<QuestionnaireRecDataBO> dataList,
                              Map<String, QesFieldDataBO> qesFieldDataBoMap,
                              QuestionnaireRecDataBO questionnaireRecDataBO) {
        QesFieldDataBO qesFieldDataBO = qesFieldDataBoMap.get(questionnaireRecDataBO.getQesField());
        if (Objects.isNull(qesFieldDataBO)) {
            return;
        }

        questionnaireRecDataBO.setRecAnswer(qesFieldDataBO.getRecAnswer());
        dataList.add(questionnaireRecDataBO);
        //单选或者多选Input
        getHideRadioInputData(dataList, qesFieldDataBoMap, questionnaireRecDataBO);
    }

    private void getRadioData(List<QuestionnaireRecDataBO> dataList, Map<String, OptionAnswer> answerMap, List<QuestionnaireRecDataBO> recDataList) {
        if (CollUtil.isEmpty(recDataList)){
            return;
        }
        List<QuestionnaireRecDataBO> inputList = recDataList.stream().map(QuestionnaireRecDataBO::getQuestionnaireRecDataBOList).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
        QuestionnaireRecDataBO questionnaireRecDataBO = getQuestionnaireRecDataBO(recDataList, answerMap);
        if (!Objects.equals(questionnaireRecDataBO.getQesField(),QuestionnaireConstant.QM)){
            dataList.add(questionnaireRecDataBO);
        }
        if (CollUtil.isEmpty(inputList)) {
            return;
        }
        getInputData(dataList, answerMap, inputList);
    }

    /**
     * 获取单选的问卷Rec数据信息
     *
     * @param recDataList 问卷Rec数据信息集合
     * @param answerMap   答案集合
     */
    private QuestionnaireRecDataBO getQuestionnaireRecDataBO(List<QuestionnaireRecDataBO> recDataList,
                                                             Map<String, OptionAnswer> answerMap) {
        //初始化单选值
        List<QuestionnaireRecDataBO> result = Lists.newArrayList();

        for (QuestionnaireRecDataBO questionnaireRecDataBO : recDataList) {
            OptionAnswer optionAnswer = answerMap.get(questionnaireRecDataBO.getOptionId());
            if (Objects.isNull(optionAnswer)) {
                continue;
            }
            result.add(questionnaireRecDataBO);
        }
        if (CollUtil.isEmpty(result)) {
            QuestionnaireRecDataBO questionnaireRecDataBO = ObjectUtil.cloneByStream(recDataList.get(0));
            questionnaireRecDataBO.setRecAnswer(StrUtil.EMPTY);
            return questionnaireRecDataBO;
        }
        return result.get(0);
    }

    /**
     * 获取单元或者多选类型的数据
     *
     * @param dataList               结果集合
     * @param answerMap              问题集合
     * @param questionnaireRecDataBO 问卷Rec数据信息
     */
    private void getCheckboxData(List<QuestionnaireRecDataBO> dataList,
                                 Map<String, OptionAnswer> answerMap,
                                 QuestionnaireRecDataBO questionnaireRecDataBO) {
        OptionAnswer optionAnswer = answerMap.get(questionnaireRecDataBO.getOptionId());
        if (Objects.isNull(optionAnswer)) {
            questionnaireRecDataBO.setRecAnswer("2");
        }
        questionnaireRecDataBO.setRecAnswer("1");
        //多选Input
        getCheckboxInputData(dataList, answerMap, questionnaireRecDataBO);
    }

    /**
     * 获取单元或者多选Input类型的数据
     *
     * @param dataList               结果集合
     * @param answerMap              问题集合
     * @param questionnaireRecDataBO 问卷Rec数据信息
     */
    private void getCheckboxInputData(List<QuestionnaireRecDataBO> dataList,
                                      Map<String, OptionAnswer> answerMap,
                                      QuestionnaireRecDataBO questionnaireRecDataBO) {
        List<QuestionnaireRecDataBO> questionnaireRecDataBOList = questionnaireRecDataBO.getQuestionnaireRecDataBOList();
        if (CollUtil.isEmpty(questionnaireRecDataBOList)) {
            dataList.add(questionnaireRecDataBO);
            return;
        }

        if (!Objects.equals(questionnaireRecDataBO.getQesField(),QuestionnaireConstant.QM)){
            questionnaireRecDataBO.setQuestionnaireRecDataBOList(null);
            dataList.add(questionnaireRecDataBO);
        }
        getInputData(dataList, answerMap, questionnaireRecDataBOList);
    }

    private void getHideRadioInputData(List<QuestionnaireRecDataBO> dataList,
                                       Map<String, QesFieldDataBO> qesFieldDataBOMap,
                                       QuestionnaireRecDataBO questionnaireRecDataBO) {
        List<QuestionnaireRecDataBO> questionnaireRecDataBOList = questionnaireRecDataBO.getQuestionnaireRecDataBOList();
        if (CollUtil.isEmpty(questionnaireRecDataBOList)) {
            return;
        }
        getHideInputData(dataList, qesFieldDataBOMap, questionnaireRecDataBOList);
    }


    @Override
    public GenerateExcelDataBO getExcelData(GenerateDataCondition generateDataCondition) {
        return null;
    }


    @Override
    public List<GenerateRecDataBO> getRecData(GenerateDataCondition generateDataCondition) {
        TwoTuple<List<Questionnaire>, List<UserQuestionRecord>> tuple = getBaseData(generateDataCondition);
        if (Objects.isNull(tuple)) {
            return Lists.newArrayList();
        }

        //获取学生类型问卷的 基础信息部分问卷ID
        Integer questionnaireId = null;
        if (Objects.nonNull(generateDataCondition.getBaseInfoType())) {
            questionnaireId = tuple.getFirst().stream()
                    .filter(questionnaire -> Objects.equals(questionnaire.getType(), generateDataCondition.getBaseInfoType().getType()))
                    .findFirst().map(Questionnaire::getId).orElse(null);
        }

        List<Integer> latestQuestionnaireIds = tuple.getFirst().stream().map(Questionnaire::getId).collect(Collectors.toList());

        //学校对应用户问卷记录
        Map<Integer, List<UserQuestionRecord>> schoolRecordMap = tuple.getSecond().stream()
                .filter(userQuestionRecord -> latestQuestionnaireIds.contains(userQuestionRecord.getQuestionnaireId()))
                .sorted(Comparator.comparing(UserQuestionRecord::getId))
                .collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));

        List<QuestionnaireQuestionRecDataBO> dataBuildList = questionnaireFacade.getDataBuildList(latestQuestionnaireIds);

        if (Objects.isNull(questionnaireId)){
            questionnaireId = latestQuestionnaireIds.get(0);
        }

        List<HideQuestionRecDataBO> hideQuestionDataBOList = questionnaireFacade.getHideQuestionnaireQuestionRec(questionnaireId);

        List<QesFieldMapping> qesFieldMappingList = questionnaireFacade.getQesFieldMappingList(latestQuestionnaireIds);

        List<String> qesFieldList = qesFieldMappingList.stream()
                .map(qesFieldMapping -> AnswerUtil.getQesFieldStr(qesFieldMapping.getQesField()))
                .collect(Collectors.toList());

        Map<Integer, Map<String, List<QuestionnaireRecDataBO>>> schoolAnswerMap = Maps.newHashMap();
        schoolRecordMap.forEach((schoolId, recordList) -> {
            List<UserQuestionnaireAnswerBO> userQuestionnaireAnswerBOList = getUserQuestionnaireAnswerBOList(recordList, hideQuestionDataBOList, generateDataCondition.getUserType());
            schoolAnswerMap.put(schoolId, getRecData(userQuestionnaireAnswerBOList, dataBuildList, qesFieldList));
        });

        Integer qesFileId = questionnaireFacade.getQesFileId(qesFieldMappingList.get(0).getQesId());
        String qesUrl = resourceFileService.getResourcePath(qesFileId);

        return schoolAnswerMap.entrySet().stream()
                .map(entry -> buildGenerateRecDataBO(qesFieldList, qesUrl, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

    }


    /**
     * 获取问卷和用户问卷记录信息
     * @param generateDataCondition 生成数据条件对象
     */
    private TwoTuple<List<Questionnaire>, List<UserQuestionRecord>> getBaseData(GenerateDataCondition generateDataCondition) {
        QuestionnaireTypeEnum mainBodyType = generateDataCondition.getMainBodyType();
        ExportCondition exportCondition = generateDataCondition.getExportCondition();
        //根据问卷类型获取问卷集合
        List<Questionnaire> questionnaireList = questionnaireFacade.getLatestQuestionnaire(mainBodyType, QuestionnaireConstant.REC_FILE);
        if (CollUtil.isEmpty(questionnaireList)) {
            log.warn("暂无此问卷类型：{}", mainBodyType.getDesc());
            return null;
        }

        //获取用户问卷记录
        List<UserQuestionRecord> userQuestionRecordList = getQuestionnaireRecordList(
                questionnaireFacade.getQuestionnaireTypeList(mainBodyType, QuestionnaireConstant.REC_FILE),
                getConditionValue(exportCondition),
                generateDataCondition.getUserType());

        userQuestionRecordList = getAnswerData(buildAnswerData(userQuestionRecordList,generateDataCondition));

        if (CollUtil.isEmpty(userQuestionRecordList)) {
            Object[] paramArray = {exportCondition.getNotificationId(), exportCondition.getPlanId(), exportCondition.getTaskId(), mainBodyType.getDesc(), generateDataCondition.getUserType()};
            log.info("notificationId:{}、planId:{}、taskId:{},问卷类型:{},用户类型:{},暂无数据", paramArray);
            return null;
        }

        return TwoTuple.of(questionnaireList, userQuestionRecordList);
    }

    /**
     * 获取用户问卷答案集合
     *
     * @param userQuestionRecordList 用户问卷记录
     * @param hideQuestionDataBOList 隐藏问题数据集合
     * @param userType               用户类型
     */
    private List<UserQuestionnaireAnswerBO> getUserQuestionnaireAnswerBOList(List<UserQuestionRecord> userQuestionRecordList,
                                                                             List<HideQuestionRecDataBO> hideQuestionDataBOList,
                                                                             Integer userType) {
        List<Integer> recordIds = userQuestionRecordList.stream().map(UserQuestionRecord::getId).collect(Collectors.toList());
        List<UserAnswer> userAnswerList = userAnswerService.getListByRecordIds(recordIds);

        Set<Integer> planStudentIds = userQuestionRecordList.stream().map(UserQuestionRecord::getUserId).collect(Collectors.toSet());
        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(planStudentIds));

        Set<Integer> schoolIds = userQuestionRecordList.stream().map(UserQuestionRecord::getSchoolId).collect(Collectors.toSet());
        List<School> schoolList = schoolService.getByIds(Lists.newArrayList(schoolIds));

        UserQuestionnaireAnswerInfoBuilder build = UserQuestionnaireAnswerInfoBuilder.builder()
                .userQuestionRecordList(userQuestionRecordList)
                .userAnswerMap(userAnswerList.stream().collect(Collectors.groupingBy(UserAnswer::getRecordId)))
                .hideQuestionDataBOList(hideQuestionDataBOList)
                .planSchoolStudentMap(planSchoolStudentList.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getStudentId, Function.identity())))
                .schoolMap(schoolList.stream().collect(Collectors.toMap(School::getId, Function.identity())))
                .userType(userType)
                .build();

        return build.dataBuild();
    }

    private GenerateRecDataBO buildGenerateRecDataBO(List<String> qesFieldList, String qesUrl, Integer schoolId, Map<String, List<QuestionnaireRecDataBO>> studentAnswersMap) {
        List<List<String>> dataList = new ArrayList<>();
        studentAnswersMap.forEach((userKey, answerList) -> dataList.add(answerList.stream()
                .map(answer->Optional.ofNullable(answer)
                        .map(questionnaireRecDataBO ->Optional.ofNullable(questionnaireRecDataBO.getRecAnswer()).orElse(StrUtil.EMPTY))
                        .orElse(StrUtil.EMPTY))
                .collect(Collectors.toList())));
        List<String> dataTxt = EpiDataUtil.mergeDataTxt(qesFieldList, dataList);
        return new GenerateRecDataBO(schoolId, qesUrl, dataTxt);
    }


    /**
     * 构建获取答案数据条件
     * @param userQuestionRecordList 用户问卷记录集合
     * @param generateDataCondition 生成数据条件
     */
    private AnswerDataBO buildAnswerData(List<UserQuestionRecord> userQuestionRecordList, GenerateDataCondition generateDataCondition){
        return new AnswerDataBO()
                .setExportCondition(generateDataCondition.getExportCondition())
                .setUserQuestionRecordList(userQuestionRecordList)
                .setGradeTypeList(generateDataCondition.getGradeTypeList());
    }

    /**
     * 获取答案数据
     * @param answerDataBO 条件实体
     */
    protected abstract List<UserQuestionRecord> getAnswerData(AnswerDataBO answerDataBO);
}

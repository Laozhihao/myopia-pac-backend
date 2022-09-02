package com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.*;
import com.wupol.myopia.business.aggregation.export.excel.domain.builder.UserAnswerProcessBuilder;
import com.wupol.myopia.business.aggregation.export.excel.domain.builder.UserQuestionnaireAnswerInfoBuilder;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.QuestionnaireFactory;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.function.ExportType;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.aggregation.export.service.ScreeningFacade;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireStatusEnum;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import com.wupol.myopia.business.core.questionnaire.domain.dos.HideQuestionRecDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionnaireDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionnaireQuestionDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.model.QesFieldMapping;
import com.wupol.myopia.business.core.questionnaire.domain.model.Questionnaire;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
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
    @Autowired
    private ScreeningFacade screeningFacade;


    private static final String REC_FILE_NAME = "%s的%s的rec文件";
    private static final String EXCEL_FILE_NAME="%s的%s的问卷数据.xlsx";

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

        //过滤作废筛查计划数据
        userQuestionRecordList =  screeningFacade.filterByPlanId(userQuestionRecordList);

        //过滤用户类型
        return userQuestionRecordList.stream()
                .filter(userQuestionRecord -> Objects.equals(userQuestionRecord.getUserType(), userType))
                .filter(userQuestionRecord -> questionnaireTypeList.contains(userQuestionRecord.getQuestionnaireType()))
                .collect(Collectors.toList());
    }


    @Override
    public String getFileName(FileNameCondition fileNameCondition) {
        QuestionnaireTypeEnum questionnaireTypeEnum = QuestionnaireTypeEnum.getQuestionnaireType(fileNameCondition.getQuestionnaireType());
        String name;
        switch (questionnaireTypeEnum){
            case PRIMARY_SCHOOL:
            case MIDDLE_SCHOOL:
            case UNIVERSITY_SCHOOL:
            case SCHOOL_ENVIRONMENT:
            case PRIMARY_SECONDARY_SCHOOLS:
            case VISION_SPINE:
                School school = schoolService.getById(fileNameCondition.getSchoolId());
                name = school.getName();
                break;
            case AREA_DISTRICT_SCHOOL:
                name = districtService.getDistrictNameByDistrictCode(fileNameCondition.getDistrictCode());
                break;
            default:
                name = StrUtil.EMPTY;
                break;
        }
        if (Objects.equals(fileNameCondition.getFileType(),QuestionnaireConstant.EXCEL_FILE)){
            return String.format(EXCEL_FILE_NAME, name, questionnaireTypeEnum.getDesc());
        }else {
            return String.format(REC_FILE_NAME, name, questionnaireTypeEnum.getDesc());
        }
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
        RecExportDTO recExportDTO = UserAnswerProcessBuilder.buildRecExportDTO(generateRecDataBO, recFileName);

        log.info("请求参数：{}", JSON.toJSONString(recExportDTO));
        CompletableFuture<RecExportVO> future = CompletableFuture.supplyAsync(() -> recServiceClient.export(recExportDTO), asyncServiceExecutor);
        try {
            RecExportVO recExportVO = future.get();
            String recPath = EpiDataUtil.getRecPath(recExportVO.getRecUrl(), fileName, recExportVO.getRecName());
            UserAnswerProcessBuilder.recFileMove(recPath,fileName,recExportVO.getRecName());
            log.info("生成rec文件成功 recName={}", recExportVO.getRecName());
        } catch (InterruptedException e) {
            log.warn("Interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("获取rec导出结果失败");
        }
    }

    /**
     * 过滤区域
     *
     * @param districtId 区域ID
     */
    protected List<Integer> filterDistrict(Integer districtId) {
        return districtService.filterDistrict(districtId);
    }


    @Override
    public List<GenerateExcelDataBO> getExcelData(GenerateDataCondition generateDataCondition) {
        TwoTuple<List<Questionnaire>, List<UserQuestionRecord>> tuple = getBaseData(generateDataCondition);
        if (Objects.isNull(tuple)) {
            return Lists.newArrayList();
        }
        UserQuestionnaireAnswerCondition userQuestionnaireAnswerCondition = getUserQuestionnaireAnswerCondition(tuple.getFirst(), generateDataCondition);

        //政府，省、地市及区（县）管理部门学校卫生工作调查表
        if (Objects.equals(generateDataCondition.getUserType(), UserType.QUESTIONNAIRE_GOVERNMENT.getType())
                && Objects.equals(generateDataCondition.getMainBodyType(),QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL)) {
            return getGovernmentExcelData(userQuestionnaireAnswerCondition,generateDataCondition, tuple.getSecond());
        }

        //学校对应用户问卷记录
        Map<Integer, List<UserQuestionRecord>> schoolRecordMap = tuple.getSecond().stream()
                .filter(userQuestionRecord -> userQuestionnaireAnswerCondition.getLatestQuestionnaireIds().contains(userQuestionRecord.getQuestionnaireId()))
                .sorted(Comparator.comparing(UserQuestionRecord::getId))
                .collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));

        //构建问卷Rec数据信息
        Map<Integer, Map<String, List<QuestionnaireDataBO>>> schoolAnswerMap = Maps.newHashMap();
        schoolRecordMap.forEach((schoolId, recordList) -> {
            userQuestionnaireAnswerCondition.setUserQuestionRecordList(recordList);
            schoolAnswerMap.put(schoolId,buildAnswerMap(generateDataCondition, userQuestionnaireAnswerCondition));
        });

        //以学校维度 ，构建导出rec文件条件信息
        return schoolAnswerMap.entrySet().stream()
                .map(entry -> UserAnswerProcessBuilder.buildGenerateExcelDataBO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

    }


    @Override
    public List<GenerateRecDataBO> getRecData(GenerateDataCondition generateDataCondition) {
        TwoTuple<List<Questionnaire>, List<UserQuestionRecord>> tuple = getBaseData(generateDataCondition);
        if (Objects.isNull(tuple)) {
            return Lists.newArrayList();
        }

        UserQuestionnaireAnswerCondition userQuestionnaireAnswerCondition = getUserQuestionnaireAnswerCondition(tuple.getFirst(), generateDataCondition);

        //政府，省、地市及区（县）管理部门学校卫生工作调查表
        if (Objects.equals(generateDataCondition.getUserType(), UserType.QUESTIONNAIRE_GOVERNMENT.getType())
                && Objects.equals(generateDataCondition.getMainBodyType(),QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL)) {
            return getGovernmentRecData(userQuestionnaireAnswerCondition,generateDataCondition, tuple.getSecond());
        }

        //学校对应用户问卷记录
        Map<Integer, List<UserQuestionRecord>> schoolRecordMap = tuple.getSecond().stream()
                .filter(userQuestionRecord -> userQuestionnaireAnswerCondition.getLatestQuestionnaireIds().contains(userQuestionRecord.getQuestionnaireId()))
                .sorted(Comparator.comparing(UserQuestionRecord::getId))
                .collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));

        //构建问卷Rec数据信息
        Map<Integer, Map<String, List<QuestionnaireDataBO>>> schoolAnswerMap = Maps.newHashMap();
        schoolRecordMap.forEach((schoolId, recordList) -> {
            userQuestionnaireAnswerCondition.setUserQuestionRecordList(recordList);
            schoolAnswerMap.put(schoolId, buildAnswerMap(generateDataCondition, userQuestionnaireAnswerCondition));
        });


        //以学校维度 ，构建导出rec文件条件信息
        return schoolAnswerMap.entrySet().stream()
                .map(entry -> UserAnswerProcessBuilder.buildGenerateRecDataBO(userQuestionnaireAnswerCondition.getQesFieldList(), userQuestionnaireAnswerCondition.getQesUrl(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

    }

    /**
     * 获取用户问卷答案条件对象
     * @param questionnaireList 问卷集合
     * @param generateDataCondition 生成数据条件
     */
    private UserQuestionnaireAnswerCondition getUserQuestionnaireAnswerCondition(List<Questionnaire> questionnaireList,GenerateDataCondition generateDataCondition){

        //获取学生类型问卷的 基础信息部分问卷ID
        Integer questionnaireId = null;
        if (Objects.nonNull(generateDataCondition.getBaseInfoType())) {
            questionnaireId = questionnaireList.stream()
                    .filter(questionnaire -> Objects.equals(questionnaire.getType(), generateDataCondition.getBaseInfoType().getType()))
                    .findFirst().map(Questionnaire::getId).orElse(null);
        }

        //最新问卷ID集合
        List<Integer> latestQuestionnaireIds = questionnaireList.stream().map(Questionnaire::getId).collect(Collectors.toList());

        //获取问卷问题rec数据结构
        List<QuestionnaireQuestionDataBO> dataBuildList = questionnaireFacade.getDataBuildList(latestQuestionnaireIds);

        //没有基础信息问卷，使用问卷ID集合第一个
        if (Objects.isNull(questionnaireId)){
            questionnaireId = latestQuestionnaireIds.get(0);
        }

        //获取隐藏数据结果
        List<HideQuestionRecDataBO> hideQuestionDataBOList = questionnaireFacade.getHideQuestionnaireQuestionRec(questionnaireId);

        //获取问卷对应的qes字段集合
        List<QesFieldMapping> qesFieldMappingList = questionnaireFacade.getQesFieldMappingList(latestQuestionnaireIds);

        //处理qes字段 满足导出rec文件的数据文件（txt文件）头部信息
        List<String> qesFieldList = qesFieldMappingList.stream()
                .map(qesFieldMapping -> AnswerUtil.getQesFieldStr(qesFieldMapping.getQesField()))
                .collect(Collectors.toList());

        UserQuestionnaireAnswerCondition userQuestionnaireAnswerCondition = new UserQuestionnaireAnswerCondition()
                .setHideQuestionDataBOList(hideQuestionDataBOList)
                .setDataBuildList(dataBuildList)
                .setQesFieldList(qesFieldList)
                .setLatestQuestionnaireIds(latestQuestionnaireIds);
        if (Objects.equals(generateDataCondition.getFileType(),QuestionnaireConstant.REC_FILE)){
            //获取qes文件的地址信息
            Integer qesFileId = questionnaireFacade.getQesFileId(qesFieldMappingList.get(0).getQesId());
            String qesUrl = resourceFileService.getResourcePath(qesFileId);
            userQuestionnaireAnswerCondition.setQesUrl(qesUrl);
        }

        return userQuestionnaireAnswerCondition;
    }

    /**
     * 政府Rec文件数据
     * @param userQuestionnaireAnswerCondition 用户问卷答案条件对象
     * @param generateDataCondition 生成导出数据条件对象
     */
    private List<GenerateRecDataBO> getGovernmentRecData(UserQuestionnaireAnswerCondition userQuestionnaireAnswerCondition,GenerateDataCondition generateDataCondition,List<UserQuestionRecord> userQuestionRecordList){

        Map<String, List<UserQuestionRecord>> governmentRecordMap = userQuestionRecordList.stream()
                .filter(userQuestionRecord -> userQuestionnaireAnswerCondition.getLatestQuestionnaireIds().contains(userQuestionRecord.getQuestionnaireId()))
                .sorted(Comparator.comparing(UserQuestionRecord::getId))
                .collect(Collectors.groupingBy(userQuestionRecord -> UserQuestionnaireAnswerInfoBuilder.getGovernmentKey(userQuestionRecord.getUserType(),userQuestionRecord.getGovId(),userQuestionRecord.getDistrictCode())));

        Map<String, Map<String, List<QuestionnaireDataBO>>> governmentAnswerMap = Maps.newHashMap();
        governmentRecordMap.forEach((key, recordList) -> {
            userQuestionnaireAnswerCondition.setUserQuestionRecordList(recordList);
            governmentAnswerMap.put(key, buildAnswerMap(generateDataCondition, userQuestionnaireAnswerCondition));
        });


        return governmentAnswerMap.entrySet().stream()
                .map(entry -> UserAnswerProcessBuilder.buildGovernmentGenerateRecDataBO(userQuestionnaireAnswerCondition.getQesFieldList(), userQuestionnaireAnswerCondition.getQesUrl(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }


    /**
     * 政府Excel文件数据
     * @param userQuestionnaireAnswerCondition 用户问卷答案条件对象
     * @param generateDataCondition 生成导出数据条件对象
     */
    private List<GenerateExcelDataBO> getGovernmentExcelData(UserQuestionnaireAnswerCondition userQuestionnaireAnswerCondition,GenerateDataCondition generateDataCondition,List<UserQuestionRecord> userQuestionRecordList){

        Map<String, List<UserQuestionRecord>> governmentRecordMap = userQuestionRecordList.stream()
                .filter(userQuestionRecord -> userQuestionnaireAnswerCondition.getLatestQuestionnaireIds().contains(userQuestionRecord.getQuestionnaireId()))
                .sorted(Comparator.comparing(UserQuestionRecord::getId))
                .collect(Collectors.groupingBy(userQuestionRecord -> UserQuestionnaireAnswerInfoBuilder.getGovernmentKey(userQuestionRecord.getUserType(),userQuestionRecord.getGovId(),userQuestionRecord.getDistrictCode())));

        Map<String, Map<String, List<QuestionnaireDataBO>>> governmentAnswerMap = Maps.newHashMap();
        governmentRecordMap.forEach((key, recordList) -> {
            userQuestionnaireAnswerCondition.setUserQuestionRecordList(recordList);
            governmentAnswerMap.put(key, buildAnswerMap(generateDataCondition, userQuestionnaireAnswerCondition));
        });


        return governmentAnswerMap.entrySet().stream()
                .map(entry -> UserAnswerProcessBuilder.buildGovernmentGenerateExcelDataBO(entry.getKey(), entry.getValue()))
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
                questionnaireFacade.getQuestionnaireTypeList(mainBodyType, generateDataCondition.getFileType()),
                getConditionValue(exportCondition),
                generateDataCondition.getUserType());

        userQuestionRecordList = getAnswerData(UserAnswerProcessBuilder.buildAnswerData(userQuestionRecordList,generateDataCondition));

        if (CollUtil.isEmpty(userQuestionRecordList)) {
            Object[] paramArray = {exportCondition.getNotificationId(), exportCondition.getPlanId(), exportCondition.getTaskId(), mainBodyType.getDesc(), generateDataCondition.getUserType()};
            log.info("notificationId:{}、planId:{}、taskId:{},问卷类型:{},用户类型:{},暂无数据", paramArray);
            return null;
        }

        return TwoTuple.of(questionnaireList, userQuestionRecordList);
    }

    /**
     * 构建答案集合
     * @param generateDataCondition 生成数据条件
     * @param userQuestionnaireAnswerCondition 用户问卷答案条件
     */
    private Map<String, List<QuestionnaireDataBO>> buildAnswerMap(GenerateDataCondition generateDataCondition, UserQuestionnaireAnswerCondition userQuestionnaireAnswerCondition) {
        List<UserQuestionnaireAnswerBO> userQuestionnaireAnswerBOList = getUserQuestionnaireAnswerBOList(generateDataCondition,userQuestionnaireAnswerCondition);
        return UserAnswerProcessBuilder.getRecData(userQuestionnaireAnswerBOList, userQuestionnaireAnswerCondition.getDataBuildList(), userQuestionnaireAnswerCondition.getQesFieldList());
    }

    /**
     * 获取用户问卷答案集合
     *
     * @param userQuestionnaireAnswerCondition 用户问卷记录条件实体
     */
    private List<UserQuestionnaireAnswerBO> getUserQuestionnaireAnswerBOList(GenerateDataCondition generateDataCondition,UserQuestionnaireAnswerCondition userQuestionnaireAnswerCondition) {
        List<HideQuestionRecDataBO> hideQuestionDataBOList = userQuestionnaireAnswerCondition.getHideQuestionDataBOList();
        List<UserQuestionRecord> userQuestionRecordList = userQuestionnaireAnswerCondition.getUserQuestionRecordList();

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
                .questionnaireTypeEnum(generateDataCondition.getMainBodyType()).userType(generateDataCondition.getUserType())
                .build();

        return build.dataBuild();
    }


    /**
     * 获取有效的用户问卷记录信息
     * @param answerDataBO 条件实体
     * @return  用户问题记录集合
     */
    protected abstract List<UserQuestionRecord> getAnswerData(AnswerDataBO answerDataBO);
}

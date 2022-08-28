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
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.business.aggregation.export.excel.domain.bo.*;
import com.wupol.myopia.business.aggregation.export.excel.domain.builder.UserAnswerProcessBuilder;
import com.wupol.myopia.business.aggregation.export.excel.domain.builder.UserQuestionnaireAnswerInfoBuilder;
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


    @Override
    public String getRecFileName(RecFileNameCondition recFileNameCondition) {
        QuestionnaireTypeEnum questionnaireTypeEnum = QuestionnaireTypeEnum.getQuestionnaireType(recFileNameCondition.getQuestionnaireType());
        String name;
        switch (questionnaireTypeEnum){
            case PRIMARY_SCHOOL:
            case MIDDLE_SCHOOL:
            case UNIVERSITY_SCHOOL:
            case SCHOOL_ENVIRONMENT:
            case PRIMARY_SECONDARY_SCHOOLS:
                School school = schoolService.getById(recFileNameCondition.getSchoolId());
                name = school.getName();
                break;
            case AREA_DISTRICT_SCHOOL:
                name = districtService.getDistrictNameByDistrictCode(recFileNameCondition.getDistrictCode());
                break;
            default:
                name = StrUtil.EMPTY;
                break;
        }
        return String.format(FILE_NAME, name, questionnaireTypeEnum.getDesc());
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
        if (Objects.isNull(districtId)) {
            return null;
        }
        List<Integer> districtIdList = districtService.getSpecificDistrictTreeAllDistrictIds(districtId);
        if (!districtIdList.contains(districtId)) {
            districtIdList.add(districtId);
        }
        return districtIdList;

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

        //政府，省、地市及区（县）管理部门学校卫生工作调查表
        if (Objects.equals(generateDataCondition.getUserType(), UserType.QUESTIONNAIRE_GOVERNMENT.getType())
                && Objects.equals(generateDataCondition.getMainBodyType(),QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL)) {
            return getGovernmentRecData(tuple,generateDataCondition);
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
        schoolRecordMap.forEach((schoolId, recordList) -> schoolAnswerMap.put(schoolId,buildAnswerMap(generateDataCondition, dataBuildList, hideQuestionDataBOList, qesFieldList, recordList)));

        Integer qesFileId = questionnaireFacade.getQesFileId(qesFieldMappingList.get(0).getQesId());
        String qesUrl = resourceFileService.getResourcePath(qesFileId);

        return schoolAnswerMap.entrySet().stream()
                .map(entry -> UserAnswerProcessBuilder.buildGenerateRecDataBO(qesFieldList, qesUrl, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

    }

    private List<GenerateRecDataBO> getGovernmentRecData(TwoTuple<List<Questionnaire>, List<UserQuestionRecord>> tuple,GenerateDataCondition generateDataCondition){

        List<Integer> latestQuestionnaireIds = tuple.getFirst().stream().map(Questionnaire::getId).collect(Collectors.toList());

        Map<String, List<UserQuestionRecord>> governmentRecordMap = tuple.getSecond().stream()
                .filter(userQuestionRecord -> latestQuestionnaireIds.contains(userQuestionRecord.getQuestionnaireId()))
                .sorted(Comparator.comparing(UserQuestionRecord::getId))
                .collect(Collectors.groupingBy(userQuestionRecord -> UserQuestionnaireAnswerInfoBuilder.getGovernmentKey(userQuestionRecord.getUserType(),userQuestionRecord.getGovId(),userQuestionRecord.getDistrictCode())));

        List<QuestionnaireQuestionRecDataBO> dataBuildList = questionnaireFacade.getDataBuildList(latestQuestionnaireIds);

        List<HideQuestionRecDataBO> hideQuestionDataBOList = questionnaireFacade.getHideQuestionnaireQuestionRec(latestQuestionnaireIds.get(0));

        List<QesFieldMapping> qesFieldMappingList = questionnaireFacade.getQesFieldMappingList(latestQuestionnaireIds);

        List<String> qesFieldList = qesFieldMappingList.stream()
                .map(qesFieldMapping -> AnswerUtil.getQesFieldStr(qesFieldMapping.getQesField()))
                .collect(Collectors.toList());

        Map<String, Map<String, List<QuestionnaireRecDataBO>>> governmentAnswerMap = Maps.newHashMap();
        governmentRecordMap.forEach((key, recordList) -> governmentAnswerMap.put(key,buildAnswerMap(generateDataCondition, dataBuildList, hideQuestionDataBOList, qesFieldList, recordList)));

        Integer qesFileId = questionnaireFacade.getQesFileId(qesFieldMappingList.get(0).getQesId());
        String qesUrl = resourceFileService.getResourcePath(qesFileId);

        return governmentAnswerMap.entrySet().stream()
                .map(entry -> UserAnswerProcessBuilder.buildGovernmentGenerateRecDataBO(qesFieldList, qesUrl, entry.getKey(), entry.getValue()))
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

        userQuestionRecordList = getAnswerData(UserAnswerProcessBuilder.buildAnswerData(userQuestionRecordList,generateDataCondition));

        if (CollUtil.isEmpty(userQuestionRecordList)) {
            Object[] paramArray = {exportCondition.getNotificationId(), exportCondition.getPlanId(), exportCondition.getTaskId(), mainBodyType.getDesc(), generateDataCondition.getUserType()};
            log.info("notificationId:{}、planId:{}、taskId:{},问卷类型:{},用户类型:{},暂无数据", paramArray);
            return null;
        }

        return TwoTuple.of(questionnaireList, userQuestionRecordList);
    }


    private Map<String, List<QuestionnaireRecDataBO>> buildAnswerMap(GenerateDataCondition generateDataCondition, List<QuestionnaireQuestionRecDataBO> dataBuildList,
                                                                     List<HideQuestionRecDataBO> hideQuestionDataBOList, List<String> qesFieldList,
                                                                     List<UserQuestionRecord> recordList) {
        UserQuestionnaireAnswerCondition userQuestionnaireAnswerCondition = new UserQuestionnaireAnswerCondition()
                .setUserQuestionRecordList(recordList)
                .setHideQuestionDataBOList(hideQuestionDataBOList)
                .setGenerateDataCondition(generateDataCondition);
        List<UserQuestionnaireAnswerBO> userQuestionnaireAnswerBOList = getUserQuestionnaireAnswerBOList(userQuestionnaireAnswerCondition);
        return UserAnswerProcessBuilder.getRecData(userQuestionnaireAnswerBOList, dataBuildList, qesFieldList);

    }

    /**
     * 获取用户问卷答案集合
     *
     * @param userQuestionnaireAnswerCondition 用户问卷记录条件实体
     */
    private List<UserQuestionnaireAnswerBO> getUserQuestionnaireAnswerBOList(UserQuestionnaireAnswerCondition userQuestionnaireAnswerCondition) {
        GenerateDataCondition generateDataCondition = userQuestionnaireAnswerCondition.getGenerateDataCondition();
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
     * 获取答案数据
     * @param answerDataBO 条件实体
     * @return  用户问题记录集合
     */
    protected abstract List<UserQuestionRecord> getAnswerData(AnswerDataBO answerDataBO);
}

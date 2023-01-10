package com.wupol.myopia.business.aggregation.screening.service;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.constant.QuestionnaireUserType;
import com.wupol.myopia.business.aggregation.screening.domain.builder.ScreeningBizBuilder;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireStatusEnum;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.GradeQuestionnaireInfo;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanSchoolDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.facade.VisionScreeningResultFacade;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 筛查学校
 *
 * @author Simple4H
 */
@Slf4j
@Service
public class ScreeningPlanSchoolBizService {

    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private UserQuestionRecordService userQuestionRecordService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private VisionScreeningResultFacade visionScreeningResultFacade;

    /**
     * 通过筛查计划ID获取所有关联的学校vo信息
     *
     * @param screeningPlanId 筛查计划ID
     * @param schoolName      学校名称
     * @return List<ScreeningPlanSchoolDTO>
     */
    public List<ScreeningPlanSchoolDTO> getSchoolVoListsByPlanId(Integer screeningPlanId, String schoolName) {
        List<ScreeningPlanSchoolDTO> screeningPlanSchools = screeningPlanSchoolService.getScreeningPlanSchools(screeningPlanId, schoolName);

        ScreeningPlan screeningPlan = screeningPlanService.findOne(new ScreeningPlan().setId(screeningPlanId));

        //学校ID对应的学生数集合
        Map<Integer, Integer> schoolIdStudentCountMap = screeningPlanSchoolStudentService.getSchoolStudentCountByScreeningPlanId(screeningPlanId);
        List<UserQuestionRecord> userQuestionRecords = userQuestionRecordService.findRecordByPlanIdAndUserType(Lists.newArrayList(screeningPlanId), QuestionnaireUserType.STUDENT.getType(),QuestionnaireStatusEnum.FINISH.getCode());
        Map<Integer, List<UserQuestionRecord>> userQuestionRecordMap = getSchoolMap(userQuestionRecords);
        Map<Integer, List<ScreeningPlanSchoolStudent>> userGradeIdMap = getGradeStudentMap(userQuestionRecords);
        Map<Integer, List<SchoolGradeExportDTO>> gradeIdMap = getGradeMap(screeningPlanSchools);

        Set<Integer> schoolIds = screeningPlanSchools.stream().map(ScreeningPlanSchoolDTO::getSchoolId).collect(Collectors.toSet());
        Map<Integer, Integer> screeningResultCountMap = visionScreeningResultFacade.getScreeningResultCountMap(screeningPlan, schoolIds);
        screeningPlanSchools.forEach(vo -> setScreeningData(screeningPlan, schoolIdStudentCountMap, userQuestionRecordMap, userGradeIdMap, gradeIdMap, screeningResultCountMap, vo));
        return screeningPlanSchools;
    }

    /**
     * 设值筛查数据
     * @param screeningPlan
     * @param schoolIdStudentCountMap
     * @param userQuestionRecordMap
     * @param userGradeIdMap
     * @param gradeIdMap
     * @param screeningResultCountMap
     * @param planSchoolDTO
     */
    private void setScreeningData(ScreeningPlan screeningPlan, Map<Integer, Integer> schoolIdStudentCountMap,
                                  Map<Integer, List<UserQuestionRecord>> userQuestionRecordMap,
                                  Map<Integer, List<ScreeningPlanSchoolStudent>> userGradeIdMap,
                                  Map<Integer, List<SchoolGradeExportDTO>> gradeIdMap,
                                  Map<Integer, Integer> screeningResultCountMap, ScreeningPlanSchoolDTO planSchoolDTO) {
        planSchoolDTO.setStudentCount(schoolIdStudentCountMap.getOrDefault(planSchoolDTO.getSchoolId(), CommonConst.ZERO));
        planSchoolDTO.setPracticalStudentCount(screeningResultCountMap.getOrDefault(planSchoolDTO.getSchoolId(), CommonConst.ZERO));
        planSchoolDTO.setScreeningProportion(MathUtil.ratio(planSchoolDTO.getPracticalStudentCount(), planSchoolDTO.getStudentCount()));
        planSchoolDTO.setScreeningSituation(ScreeningBizBuilder.getSituation(screeningResultCountMap.getOrDefault(planSchoolDTO.getSchoolId(), CommonConst.ZERO),screeningPlan));
        planSchoolDTO.setQuestionnaireStudentCount(getQuestionnaireStudentCount(userQuestionRecordMap,planSchoolDTO));
        planSchoolDTO.setQuestionnaireProportion(MathUtil.ratio(planSchoolDTO.getQuestionnaireStudentCount(),planSchoolDTO.getStudentCount()));
        planSchoolDTO.setQuestionnaireSituation(ScreeningBizBuilder.getCountBySchool(screeningPlan, planSchoolDTO.getSchoolId(), userQuestionRecordMap));
        if (!CollectionUtils.isEmpty(gradeIdMap.get(planSchoolDTO.getSchoolId())) && planSchoolDTO.getStudentCount() != 0) {
            planSchoolDTO.setGradeQuestionnaireInfos(GradeQuestionnaireInfo.buildGradeInfo(planSchoolDTO.getSchoolId(), gradeIdMap, userGradeIdMap,Boolean.TRUE));
        }
    }

    /**
     * 获取问卷学生数
     * @param userQuestionRecordMap
     * @param screeningPlanSchoolDTO
     */
    private Integer getQuestionnaireStudentCount(Map<Integer, List<UserQuestionRecord>> userQuestionRecordMap,ScreeningPlanSchoolDTO screeningPlanSchoolDTO){
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordMap.get(screeningPlanSchoolDTO.getSchoolId());
        if (CollUtil.isEmpty(userQuestionRecordList)){
            return 0;
        }
        return (int)userQuestionRecordList.stream().map(UserQuestionRecord::getStudentId).distinct().count();
    }

    /**
     * 获取学校ID对应的年级集合
     *
     * @param screeningPlanSchoolList 筛查计划关联的学校集合
     */
    private Map<Integer, List<SchoolGradeExportDTO>> getGradeMap(List<ScreeningPlanSchoolDTO> screeningPlanSchoolList){
        Map<Integer, List<SchoolGradeExportDTO>> gradeMap =Maps.newHashMap();
        if (CollectionUtils.isEmpty(screeningPlanSchoolList)){
            return gradeMap;
        }
        Set<Integer> schoolIds = screeningPlanSchoolList.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(schoolIds)){
            return schoolGradeService.getBySchoolIds(Lists.newArrayList(schoolIds)).stream().collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolId));
        }
        return gradeMap;
    }

    /**
     * 获取年级ID对应的学生集合
     * @param userQuestionRecords 用户问卷记录集合
     */
    private Map<Integer, List<ScreeningPlanSchoolStudent>> getGradeStudentMap(List<UserQuestionRecord> userQuestionRecords){
        Map<Integer, List<ScreeningPlanSchoolStudent>> gradeStudentMap =Maps.newHashMap();
        if (CollectionUtils.isEmpty(userQuestionRecords)){
            return gradeStudentMap;
        }
        List<UserQuestionRecord> finishList = userQuestionRecords.stream()
                .filter(userQuestionRecord -> Objects.equals(userQuestionRecord.getStatus(), QuestionnaireStatusEnum.FINISH.getCode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(finishList)){
            return gradeStudentMap;
        }
        Set<Integer> planStudentIds = userQuestionRecords.stream().map(UserQuestionRecord::getUserId).collect(Collectors.toSet());
        return screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(planStudentIds)).stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getGradeId));
    }

    /**
     * 获取学校ID对应的用户问卷记录集合
     * @param userQuestionRecords 用户问卷记录集合
     */
    private Map<Integer, List<UserQuestionRecord>> getSchoolMap(List<UserQuestionRecord> userQuestionRecords){
        Map<Integer, List<UserQuestionRecord>> schoolMap =Maps.newHashMap();
        if (CollectionUtils.isEmpty(userQuestionRecords)){
            return schoolMap;
        }
        List<UserQuestionRecord> finishList = userQuestionRecords.stream()
                .filter(userQuestionRecord -> Objects.equals(userQuestionRecord.getStatus(), QuestionnaireStatusEnum.FINISH.getCode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(finishList)){
            return schoolMap;
        }
        return userQuestionRecords.stream().collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));
    }

    /**
     * 查询筛查计划下有学生数据的学校
     *
     * @param screeningPlanId 筛查计划ID
     * @param schoolName 学校名称
     * @return List<ScreeningPlanSchoolDTO>
     */
    public List<ScreeningPlanSchoolDTO> querySchoolsInfoInPlanHaveStudent(Integer screeningPlanId, String schoolName) {
        List<ScreeningPlanSchoolDTO> screeningPlanSchools = getSchoolVoListsByPlanId(screeningPlanId,schoolName);

        List<Integer> schoolIds = screeningPlanSchoolStudentService.findSchoolIdsByPlanId(screeningPlanId);

        if (CollectionUtils.isEmpty(schoolIds)) {
            return new ArrayList<>();
        }
        return screeningPlanSchools.stream().filter(s -> schoolIds.contains(s.getSchoolId())).collect(Collectors.toList());
    }

    /**
     * 通过筛查计划ID获取所有关联的学校vo信息(有筛查数据)
     *
     * @param screeningPlanId 筛查计划ID
     * @param schoolName      学校名称
     * @return List<ScreeningPlanSchoolDTO>
     */
    public List<ScreeningPlanSchoolDTO> getHaveResultSchool(Integer screeningPlanId, String schoolName) {
        List<ScreeningPlanSchoolDTO> schoolList = getSchoolVoListsByPlanId(screeningPlanId, schoolName);
        List<Integer> schoolIds = visionScreeningResultService.getBySchoolIdPlanId(screeningPlanId);
        if (CollectionUtils.isEmpty(schoolIds)) {
            return new ArrayList<>();
        }
        return schoolList.stream().filter(s -> schoolIds.contains(s.getSchoolId())).collect(Collectors.toList());
    }
}

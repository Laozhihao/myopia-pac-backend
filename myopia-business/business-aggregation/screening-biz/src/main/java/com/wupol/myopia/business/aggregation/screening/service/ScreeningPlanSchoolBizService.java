package com.wupol.myopia.business.aggregation.screening.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.constant.QuestionnaireUserType;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.questionnaire.service.UserQuestionRecordService;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.GradeQuestionnaireInfo;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanSchoolDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
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
    private StudentService studentService;

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
        Map<Integer, Long> schoolIdStudentCountMap = screeningPlanSchoolStudentService.getSchoolStudentCountByScreeningPlanId(screeningPlanId);
        List<UserQuestionRecord> userQuestionRecords = userQuestionRecordService.findRecordByPlanIdAndUserType(Lists.newArrayList(screeningPlanId), QuestionnaireUserType.STUDENT.getType());
        Set<Integer> studentIds = userQuestionRecords.stream().map(UserQuestionRecord::getStudentId).collect(Collectors.toSet());
        Map<Integer, List<UserQuestionRecord>> schoolMap = userQuestionRecords.stream().collect(Collectors.groupingBy(UserQuestionRecord::getSchoolId));
        Map<Integer, List<Student>> userGradeIdMap = CollectionUtils.isEmpty(studentIds) ? Maps.newHashMap() : studentService.getByIds(studentIds)
                .stream().collect(Collectors.groupingBy(Student::getGradeId));

        Map<Integer, List<SchoolGradeExportDTO>> gradeIdMap = schoolGradeService.getBySchoolIds(screeningPlanSchools.stream().map(ScreeningPlanSchool::getSchoolId).collect(Collectors.toList())).stream().collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolId));

        // TODO：不在循环内查询数据库
        screeningPlanSchools.forEach(vo -> {
            vo.setStudentCount(schoolIdStudentCountMap.getOrDefault(vo.getSchoolId(), 0L).intValue());
            vo.setPracticalStudentCount(visionScreeningResultService.getBySchoolIdAndOrgIdAndPlanId(vo.getSchoolId(), vo.getScreeningOrgId(), vo.getScreeningPlanId()).size());
            BigDecimal num = MathUtil.divide(vo.getPracticalStudentCount(), vo.getStudentCount());
            vo.setScreeningProportion(num.equals(BigDecimal.ZERO) ? CommonConst.PERCENT_ZERO : num.toString() + "%");
            vo.setScreeningSituation(findSituation(vo.getSchoolId(), screeningPlan));
            // 完成数
            // 总数占比
            Map<Integer, List<UserQuestionRecord>> schoolStudentMap = CollectionUtils.isEmpty(schoolMap.get(vo.getSchoolId())) ? Maps.newHashMap() : schoolMap.get(vo.getSchoolId()).stream().collect(Collectors.groupingBy(UserQuestionRecord::getStudentId));
            vo.setQuestionnaireStudentCount(schoolStudentMap.keySet().size());
            if (vo.getStudentCount() == 0) {
                vo.setQuestionnaireProportion(CommonConst.PERCENT_ZERO);
            } else {
                BigDecimal questionNum = MathUtil.divide(vo.getQuestionnaireStudentCount(), vo.getStudentCount());
                vo.setQuestionnaireProportion(questionNum.equals(BigDecimal.ZERO) ? CommonConst.PERCENT_ZERO : questionNum.toString() + "%");
            }
            vo.setQuestionnaireSituation(getCountBySchool(screeningPlan, vo.getSchoolId(), schoolMap));
            if (!CollectionUtils.isEmpty(gradeIdMap.get(vo.getSchoolId())) && vo.getStudentCount() != 0) {
                vo.setGradeQuestionnaireInfos(GradeQuestionnaireInfo.buildGradeInfo(vo.getSchoolId(), gradeIdMap, userGradeIdMap));
            }
        });
        return screeningPlanSchools;
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

    /**
     * 获得问卷完成学校的状态
     *
     * @return
     * @throws IOException
     */
    public String getCountBySchool(ScreeningPlan plan, Integer schoolId, Map<Integer, List<UserQuestionRecord>> userRecordToStudentEnvironmentMap) {
        if (plan.getEndTime().getTime() <= System.currentTimeMillis()) {
            return ScreeningPlanSchool.END;
        } else if (CollectionUtils.isEmpty(userRecordToStudentEnvironmentMap.get(schoolId))) {
            return ScreeningPlanSchool.NOT_START;
        } else if (!userRecordToStudentEnvironmentMap.get(schoolId).isEmpty()) {
            return ScreeningPlanSchool.IN_PROGRESS;
        }
        return ScreeningPlanSchool.IN_PROGRESS;
    }

    public String findSituation(Integer schoolId, ScreeningPlan screeningPlan) {
        if (DateUtil.betweenDay(screeningPlan.getEndTime(), new Date()) > 0){
            return ScreeningPlanSchool.END;
        }
        int count = visionScreeningResultService.count(new VisionScreeningResult().setPlanId(screeningPlan.getId()).setSchoolId(schoolId));
        return count > 0 ? ScreeningPlanSchool.IN_PROGRESS : ScreeningPlanSchool.NOT_START;
    }
}

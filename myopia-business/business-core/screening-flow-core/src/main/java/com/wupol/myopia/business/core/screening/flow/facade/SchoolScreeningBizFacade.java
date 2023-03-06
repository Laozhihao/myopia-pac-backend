package com.wupol.myopia.business.core.screening.flow.facade;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.facade.SchoolBizFacade;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.builder.ScreeningBizBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校筛查业务门面
 *
 * @author hang.yuan 2022/9/26 17:26
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SchoolScreeningBizFacade {

    private final SchoolService schoolService;
    private final ScreeningPlanSchoolService screeningPlanSchoolService;
    private final ScreeningPlanService screeningPlanService;
    private final ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    private final SchoolBizFacade schoolBizFacade;


    /**
     * 往筛查计划新增学生 (自动新增)
     * @param schoolStudent 学校学生
     * @param isAdd 是否新增的学生
     */
    @Transactional(rollbackFor = Exception.class)
    public void addScreeningStudent(SchoolStudent schoolStudent, Boolean isAdd){
        if (Objects.isNull(schoolStudent)){
            return;
        }

        //获取有效的筛查计划
        List<ScreeningPlan> screeningPlanList = getEffectiveScreeningPlans(schoolStudent.getSchoolId());
        if (CollUtil.isEmpty(screeningPlanList)) {
            return;
        }
        Set<Integer> planIds = screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
        List<ScreeningPlanSchool> screeningPlanSchoolList = screeningPlanSchoolService.listByPlanIdsAndSchoolId(Lists.newArrayList(planIds), schoolStudent.getSchoolId());
        Map<Integer, ScreeningPlanSchool> planSchoolMap = screeningPlanSchoolList.stream().collect(Collectors.toMap(ScreeningPlanSchool::getScreeningPlanId, Function.identity()));

        //组装数据，更新筛查计划学生数和新增筛查学生
        School school = schoolService.getById(schoolStudent.getSchoolId());
        screeningPlanList.forEach(screeningPlan -> {
            if (Objects.equals(isAdd,Boolean.TRUE)){
                addScreeningStudent(schoolStudent, planSchoolMap, school, screeningPlan);
            }else {
                updateScreeningStudent(schoolStudent, planSchoolMap, school, screeningPlan);
            }
        });

    }

    /**
     * 获取有效的筛查计划
     * @param schoolId 学校ID
     */
    private List<ScreeningPlan> getEffectiveScreeningPlans(Integer schoolId) {
        // 获取筛查计划（包括自主筛查和协助筛查）
        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService.getBySchoolId(schoolId);
        if (CollUtil.isEmpty(planSchoolList)){
            return Lists.newArrayList();
        }
        List<ScreeningPlan> screeningPlanList = screeningPlanService.getNotReleaseAndReleasePlanByPlanIdList(planSchoolList.stream().map(ScreeningPlanSchool::getScreeningPlanId).collect(Collectors.toList()));
        if (CollUtil.isEmpty(screeningPlanList)){
            return Lists.newArrayList();
        }
        //获取有效期的筛查计划
        screeningPlanList = screeningPlanList.stream()
                .filter(screeningPlan -> new Date().before(screeningPlan.getEndTime()))
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(screeningPlanList)){
            return Lists.newArrayList();
        }
        return screeningPlanList;
    }

    /**
     * 往筛查计划新增学生 (自动新增)
     * @param schoolStudent
     * @param planSchoolMap
     * @param school
     * @param screeningPlan
     */
    public void addScreeningStudent(SchoolStudent schoolStudent, Map<Integer, ScreeningPlanSchool> planSchoolMap, School school, ScreeningPlan screeningPlan) {
        if (Objects.equals(validScreeningStudent(schoolStudent, planSchoolMap, screeningPlan),Boolean.TRUE)) {
            return;
        }
        List<ScreeningPlanSchoolStudent> planSchoolList = getScreeningPlanSchoolStudent(screeningPlan.getId(), Lists.newArrayList(schoolStudent), school);
        screeningPlan.setStudentNumbers(screeningPlan.getStudentNumbers() + planSchoolList.size());
        screeningPlanService.savePlanInfo(screeningPlan, null, planSchoolList);
        Object[] paramArr = new Object[]{screeningPlan.getId(),school.getId(),schoolStudent.getGradeId(),schoolStudent.getId()};
        log.info("自动新增筛查学生，planId={},schoolId={},gradeId={},schoolStudentId={}",paramArr);
    }


    /**
     * 修改学生信息同步更新筛查计划学生
     * @param schoolStudent
     * @param planSchoolMap
     * @param school
     * @param screeningPlan
     */
    public void updateScreeningStudent(SchoolStudent schoolStudent, Map<Integer, ScreeningPlanSchool> planSchoolMap, School school, ScreeningPlan screeningPlan) {
        if (Objects.equals(validScreeningStudent(schoolStudent, planSchoolMap, screeningPlan),Boolean.TRUE)) {
            return;
        }
        List<ScreeningPlanSchoolStudent> planSchoolList = getScreeningPlanSchoolStudent(screeningPlan.getId(), Lists.newArrayList(schoolStudent), school);
        if (CollUtil.isEmpty(planSchoolList)){
            return;
        }
        screeningPlanSchoolStudentService.saveOrUpdateBatch(planSchoolList);
        Object[] paramArr = new Object[]{screeningPlan.getId(),school.getId(),schoolStudent.getGradeId(),schoolStudent.getId()};
        log.info("更新筛查学生，planId={},schoolId={},gradeId={},schoolStudentId={}",paramArr);
    }

    /**
     * 校验筛查学生
     * @param schoolStudent
     * @param planSchoolMap
     * @param screeningPlan
     */
    private boolean validScreeningStudent(SchoolStudent schoolStudent, Map<Integer, ScreeningPlanSchool> planSchoolMap, ScreeningPlan screeningPlan) {
        ScreeningPlanSchool screeningPlanSchool = planSchoolMap.get(screeningPlan.getId());
        if (Objects.isNull(screeningPlanSchool)) {
            return true;
        }
        // 如果学生的年级没有在计划内，则不需要同步
        List<Integer> screeningGradeIds = ScreeningBizBuilder.getScreeningGradeIds(screeningPlanSchool.getScreeningGradeIds());
        if (!screeningGradeIds.contains(schoolStudent.getGradeId())) {
            return true;
        }
        return false;
    }

    /**
     * 获取筛查计划学校学生
     *
     * @param screeningPlanId   筛查计划ID
     * @param schoolStudentList 学校学生集合
     * @param school            学校信息
     */
    public List<ScreeningPlanSchoolStudent> getScreeningPlanSchoolStudent(Integer screeningPlanId,List<SchoolStudent> schoolStudentList , School school){
        Set<Integer> gradeIds = schoolStudentList.stream().map(SchoolStudent::getGradeId).collect(Collectors.toSet());
        TwoTuple<Map<Integer, SchoolGrade>, Map<Integer, SchoolClass>> schoolGradeAndClassMap = schoolBizFacade.getSchoolGradeAndClass(Lists.newArrayList(gradeIds));
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentDbList=null;
        if (Objects.nonNull(screeningPlanId)){
            screeningPlanSchoolStudentDbList = screeningPlanSchoolStudentService.getByScreeningPlanId(screeningPlanId,Boolean.FALSE);
        }
        return ScreeningBizBuilder.getScreeningPlanSchoolStudentList(screeningPlanId, schoolStudentList, school, schoolGradeAndClassMap.getFirst(), screeningPlanSchoolStudentDbList);
    }

}

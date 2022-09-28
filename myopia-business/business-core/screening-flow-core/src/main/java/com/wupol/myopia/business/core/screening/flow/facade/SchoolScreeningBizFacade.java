package com.wupol.myopia.business.core.screening.flow.facade;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.facade.SchoolBizFacade;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningOrgTypeEnum;
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

        if (Objects.equals(Boolean.FALSE,isAdd) || Objects.isNull(schoolStudent)){
            return;
        }
        //获取有效的筛查计划
        List<ScreeningPlan> screeningPlanList = getEffectiveScreeningPlans(schoolStudent);
        if (CollUtil.isEmpty(screeningPlanList)) {
            return;
        }
        Set<Integer> planIds = screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
        List<ScreeningPlanSchool> screeningPlanSchoolList = screeningPlanSchoolService.listByPlanIdsAndSchoolId(Lists.newArrayList(planIds), schoolStudent.getSchoolId());
        Map<Integer, ScreeningPlanSchool> planSchoolMap = screeningPlanSchoolList.stream().collect(Collectors.toMap(ScreeningPlanSchool::getScreeningPlanId, Function.identity()));

        //组装数据，更新筛查计划学生数和新增筛查学生
        School school = schoolService.getById(schoolStudent.getSchoolId());
        screeningPlanList.forEach(screeningPlan -> addScreeningStudent(schoolStudent, planSchoolMap, school, screeningPlan));

    }

    /**
     * 获取有效的筛查计划
     * @param schoolStudent 学校学生
     */
    private List<ScreeningPlan> getEffectiveScreeningPlans(SchoolStudent schoolStudent) {
        //机构ID和机构类型查询筛查计划
        List<ScreeningPlan> screeningPlanList = screeningPlanService.getByOrgIdAndOrgType(schoolStudent.getSchoolId(), ScreeningOrgTypeEnum.SCHOOL.getType());
        if (CollUtil.isEmpty(screeningPlanList)){
            return Lists.newArrayList();
        }
        //获取有效期的筛查计划
        screeningPlanList = screeningPlanList.stream()
                .filter(screeningPlan -> DateUtil.isIn(new Date(), screeningPlan.getStartTime(), screeningPlan.getEndTime()))
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
        ScreeningPlanSchool screeningPlanSchool = planSchoolMap.get(screeningPlan.getId());
        if (Objects.isNull(screeningPlanSchool)){
            return;
        }
        List<Integer> screeningGradeIds = ScreeningBizBuilder.getScreeningGradeIds(screeningPlanSchool.getScreeningGradeIds());
        if (!screeningGradeIds.contains(schoolStudent.getGradeId())){
            return;
        }
        TwoTuple<List<ScreeningPlanSchoolStudent>, List<Integer>> screeningPlanSchoolStudent = getScreeningPlanSchoolStudent(screeningPlan.getId(), Lists.newArrayList(schoolStudent), school, Boolean.TRUE);
        screeningPlan.setStudentNumbers(screeningPlan.getStudentNumbers()+screeningPlanSchoolStudent.getFirst().size());
        screeningPlanService.savePlanInfo(screeningPlan,null,screeningPlanSchoolStudent);
        Object[] paramArr = new Object[]{screeningPlan.getId(),school.getId(),schoolStudent.getGradeId(),schoolStudent.getId()};
        log.info("自动新增筛查学生，plan={},schoolId={},gradeId={},schoolStudentId={}",paramArr);
    }

    /**
     * 获取筛查计划学校学生
     * @param screeningPlanId 筛查计划ID
     * @param schoolStudentList 学校学生集合
     * @param school 学校信息
     * @param isAdd 是否新增
     */
    public TwoTuple<List<ScreeningPlanSchoolStudent>, List<Integer>> getScreeningPlanSchoolStudent(Integer screeningPlanId,List<SchoolStudent> schoolStudentList , School school,Boolean isAdd){
        Set<Integer> gradeIds = schoolStudentList.stream().map(SchoolStudent::getGradeId).collect(Collectors.toSet());
        TwoTuple<Map<Integer, SchoolGrade>, Map<Integer, SchoolClass>> schoolGradeAndClassMap = schoolBizFacade.getSchoolGradeAndClass(Lists.newArrayList(gradeIds));
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentDbList=null;
        if (Objects.nonNull(screeningPlanId)){
            screeningPlanSchoolStudentDbList = screeningPlanSchoolStudentService.getByScreeningPlanId(screeningPlanId,Boolean.FALSE);
        }
        return ScreeningBizBuilder.getScreeningPlanSchoolStudentList(schoolStudentList, school, schoolGradeAndClassMap.getFirst(), schoolGradeAndClassMap.getSecond(), screeningPlanSchoolStudentDbList,isAdd);
    }

}

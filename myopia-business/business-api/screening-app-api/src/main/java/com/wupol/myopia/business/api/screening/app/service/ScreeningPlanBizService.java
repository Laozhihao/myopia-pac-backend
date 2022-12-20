package com.wupol.myopia.business.api.screening.app.service;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.common.utils.exception.ManagementUncheckedException;
import com.wupol.myopia.business.common.utils.util.AgeUtil;
import com.wupol.myopia.business.common.utils.util.SerializationUtil;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentCommonDiseaseIdService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/4/26
 **/
@Service
public class ScreeningPlanBizService {

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StudentCommonDiseaseIdService studentCommonDiseaseIdService;
    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    /**
     * 从学生中插入
     *
     * @param currentUser
     * @param student
     * @param gradeName
     * @param clazzName
     * @param schoolName
     * @param schoolId
     * @param currentPlan
     */
    public void insertWithStudent(CurrentUser currentUser, Student student, String gradeName, String clazzName, String schoolName, String schoolNo, Integer schoolDistrictId, Integer schoolId, ScreeningPlan currentPlan,String passport) {
        ScreeningPlanSchoolStudent screeningPlanSchoolStudent = new ScreeningPlanSchoolStudent();
        Long screeningCode = ScreeningCodeGenerator.nextId();
        screeningPlanSchoolStudent.setIdCard(student.getIdCard())
                .setSrcScreeningNoticeId(currentPlan.getSrcScreeningNoticeId())
                .setScreeningTaskId(currentPlan.getScreeningTaskId())
                .setScreeningPlanId(currentPlan.getId())
                .setScreeningOrgId(currentUser.getOrgId())
                .setPlanDistrictId(currentPlan.getDistrictId())
                .setSchoolDistrictId(schoolDistrictId)
                .setSchoolId(schoolId)
                .setSchoolName(schoolName)
                .setStudentId(student.getId())
                .setPassport(passport)
                .setScreeningCode(screeningCode);

        screeningPlanSchoolStudent.setStudentName(student.getName())
                .setGradeId(student.getGradeId())
                .setGradeType(GradeCodeEnum.getByName(gradeName).getType())
                .setClassId(student.getClassId())
                .setSchoolName(schoolName)
                .setBirthday(student.getBirthday())
                .setGender(student.getGender())
                .setStudentAge(AgeUtil.countAge(student.getBirthday()))
                .setStudentSituation(SerializationUtil.serializeWithoutException(student))
                .setStudentNo(student.getSno())
                .setScreeningCode(screeningCode);
        // 设置常见病ID
        if (ScreeningTypeEnum.isCommonDiseaseScreeningType(currentPlan.getScreeningType())) {
            screeningPlanSchoolStudent.setCommonDiseaseId(studentCommonDiseaseIdService.getStudentCommonDiseaseId(schoolDistrictId, schoolId, student.getGradeId(), student.getClassId(), currentPlan.getStartTime()));
        }
        screeningPlanSchoolStudentService.save(screeningPlanSchoolStudent);
        screeningPlanService.updateStudentNumbers(currentUser.getId(), currentPlan.getId(), screeningPlanSchoolStudentService.getCountByScreeningPlanId(currentPlan.getId()));
    }


    /**
     * 获取该筛查机构目前的筛查学校
     *
     * @param schoolName 学校名称
     * @param deptId 机构ID
     * @return 学校列表
     */
    public List<School> getSchoolByOrgId(String schoolName, Integer deptId,Integer channel) {
        if (deptId == null) {
            throw new ManagementUncheckedException("deptId 不能为空");
        }

        List<Integer> schoolIds = screeningPlanService.getReleasePlanSchoolIdByScreeningOrgId(deptId, channel);
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        return schoolService.getSchoolByIdsAndName(schoolIds,schoolName);
    }

    /**
     * 查询某个筛查机构下的学校的学生
     *
     * @param screeningOrgId 机构Id
     * @param schoolId       学校Id
     *
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getPlanSchoolStudent(Integer screeningOrgId, Integer schoolId) {
        ScreeningPlanSchool planSchool = screeningPlanSchoolService.getReleasePlanByScreeningOrgIdAndSchoolId(screeningOrgId, schoolId);
        if (Objects.isNull(planSchool)) {
            return new ArrayList<>();
        }
        List<ScreeningPlanSchoolStudent> planStudents = screeningPlanSchoolStudentService.getByPlanIdAndSchoolId(planSchool.getScreeningPlanId(), planSchool.getSchoolId());
        if (CollectionUtils.isEmpty(planStudents)) {
            return new ArrayList<>();
        }
        // 特殊处理App的学生Id
        planStudents.forEach(planStudent -> planStudent.setStudentId(planStudent.getId()));
        return planStudents;
    }
}

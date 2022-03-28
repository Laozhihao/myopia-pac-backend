package com.wupol.myopia.migrate.service;

import cn.hutool.core.date.DateUtil;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.SourceClientEnum;
import com.wupol.myopia.business.core.common.constant.ArtificialStatusConstant;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.util.ScreeningCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 虚拟学生
 *
 * @Author HaoHao
 * @Date 2022/3/27
 **/
@Service
public class VirtualStudentService {

    @Autowired
    private StudentService studentService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private SchoolService schoolService;

    /**
     * 创建虚拟学生
     *
     * @param plan
     * @param schoolId
     * @param gradeId
     * @param gradeName
     * @param classId
     * @param className
     * @return com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent
     **/
    public ScreeningPlanSchoolStudent createVirtualStudent(ScreeningPlan plan, Integer schoolId, Integer gradeId, String gradeName, Integer classId, String className) {
        GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByName(gradeName);
        Date birthday = SchoolAge.getBirthdayBySchoolAgeType(gradeCodeEnum.getType());
        Long screeningCode = ScreeningCodeGenerator.nextId();
        School school = schoolService.getById(schoolId);
        // 创建管理端学生
        Student student = new Student();
        student.setSchoolId(schoolId);
        student.setCreateUserId(1);
        student.setGradeId(gradeId);
        student.setClassId(classId);
        student.setName(String.valueOf(screeningCode));
        student.setGender(GenderEnum.MALE.type);
        student.setSourceClient(SourceClientEnum.SCREENING_PLAN.type);
        student.setGradeType(gradeCodeEnum.getType());
        student.setBirthday(birthday);
        studentService.save(student);
        // 创建筛查计划学生
        ScreeningPlanSchoolStudent planSchoolStudent = new ScreeningPlanSchoolStudent();
        planSchoolStudent.setSrcScreeningNoticeId(plan.getSrcScreeningNoticeId());
        planSchoolStudent.setScreeningTaskId(plan.getScreeningTaskId());
        planSchoolStudent.setScreeningPlanId(plan.getId());
        planSchoolStudent.setScreeningOrgId(plan.getScreeningOrgId());
        planSchoolStudent.setPlanDistrictId(plan.getDistrictId());
        planSchoolStudent.setSchoolDistrictId(school.getDistrictId());
        planSchoolStudent.setSchoolId(school.getId());
        planSchoolStudent.setSchoolName(school.getName());
        planSchoolStudent.setGradeId(gradeId);
        planSchoolStudent.setGradeName(gradeName);
        planSchoolStudent.setClassId(classId);
        planSchoolStudent.setClassName(className);
        planSchoolStudent.setStudentId(student.getId());
        planSchoolStudent.setGradeType(gradeCodeEnum.getType());
        planSchoolStudent.setBirthday(birthday);
        planSchoolStudent.setGender(GenderEnum.MALE.type);
        planSchoolStudent.setStudentAge(DateUtil.ageOfNow(birthday));
        planSchoolStudent.setStudentName(student.getName());
        planSchoolStudent.setArtificial(ArtificialStatusConstant.Artificial);
        planSchoolStudent.setScreeningCode(screeningCode);
        screeningPlanSchoolStudentService.save(planSchoolStudent);
        // 更新筛查计划学生总数
        screeningPlanService.updateStudentNumbers(1, plan.getId(), screeningPlanSchoolStudentService.getCountByScreeningPlanId(plan.getId()));
        return planSchoolStudent;
    }
}

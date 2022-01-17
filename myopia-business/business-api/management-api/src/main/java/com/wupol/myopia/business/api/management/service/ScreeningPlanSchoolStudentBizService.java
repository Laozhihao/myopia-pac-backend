package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.date.DateUtil;
import com.wupol.framework.core.util.DateFormatUtil;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.api.management.domain.dto.MockStudentRequestDTO;
import com.wupol.myopia.business.api.management.domain.dto.PlanStudentRequestDTO;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Author wulizhou
 * @Date 2021/4/25 10:34
 */
@Service
public class ScreeningPlanSchoolStudentBizService {

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private ScreeningPlanService screeningPlanService;



    /**
     * 生成虚拟学生
     *
     * @param requestDTO      请求入参
     * @param screeningPlanId 筛查计划Id
     * @param schoolId        学校Id
     * @param currentUser     登录用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void initMockStudent(MockStudentRequestDTO requestDTO, Integer screeningPlanId,
                                Integer schoolId, CurrentUser currentUser) {
        Integer studentTotal = requestDTO.getStudentTotal();
        if (Objects.isNull(studentTotal) || studentTotal > 500) {
            throw new BusinessException("学生总数异常");
        }
        School school = schoolService.getById(schoolId);
        ScreeningPlan plan = screeningPlanService.getById(screeningPlanId);

        List<MockStudentRequestDTO.GradeItem> gradeItem = requestDTO.getGradeItem();
        if (!CollectionUtils.isEmpty(gradeItem)) {
            gradeItem.forEach(schoolGrade -> {
                List<MockStudentRequestDTO.ClassItem> classItem = schoolGrade.getClassItem();
                classItem.forEach(schoolClass -> {
                    List<Student> mockStudentList = new ArrayList<>(studentTotal);
                    List<ScreeningPlanSchoolStudent> mockPlanStudentList = new ArrayList<>(studentTotal);
                    mockStudent(studentTotal, school, schoolGrade, schoolClass, mockStudentList, currentUser);
                    mockPlanStudent(studentTotal, school, plan, schoolGrade, schoolClass, mockStudentList, mockPlanStudentList);
                });
            });
        }
        // 筛查学生数
        screeningPlanService.updateStudentNumbers(currentUser.getId(), plan.getId(), screeningPlanSchoolStudentService.getCountByScreeningPlanId(plan.getId()));
    }

    /**
     * 多端管理生成虚拟学生
     *
     * @param studentTotal    需要生成的学生数
     * @param school          学校
     * @param schoolGrade     年级
     * @param schoolClass     班级
     * @param mockStudentList 多端管理学生列表
     * @param currentUser     登录用户
     */
    private void mockStudent(Integer studentTotal, School school,
                             MockStudentRequestDTO.GradeItem schoolGrade, MockStudentRequestDTO.ClassItem schoolClass,
                             List<Student> mockStudentList, CurrentUser currentUser) {
        for (int i = 0; i < studentTotal; i++) {
            Student student = new Student();
            student.setSchoolId(school.getId());
            student.setCreateUserId(currentUser.getId());
            student.setGradeId(schoolGrade.getGradeId());
            student.setClassId(schoolClass.getClassId());
            student.setName(String.valueOf(ScreeningCodeGenerator.nextId()));
            student.setGender(GenderEnum.MALE.type);
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByName(schoolGrade.getGradeName());
            Date date = getDateByGrade(gradeCodeEnum);
            student.setGradeType(gradeCodeEnum.getType());
            student.setBirthday(date);
            mockStudentList.add(student);
        }
        studentService.saveOrUpdateBatch(mockStudentList);
    }

    /**
     * 生成计划学生数据
     *
     * @param studentTotal        需要生成的学生数
     * @param school              学校
     * @param plan                计划
     * @param schoolGrade         年级
     * @param schoolClass         班级
     * @param mockStudentList     多端管理学生列表
     * @param mockPlanStudentList 计划学生
     */
    private void mockPlanStudent(Integer studentTotal, School school, ScreeningPlan plan,
                                 MockStudentRequestDTO.GradeItem schoolGrade, MockStudentRequestDTO.ClassItem schoolClass,
                                 List<Student> mockStudentList, List<ScreeningPlanSchoolStudent> mockPlanStudentList) {
        for (int i = 0; i < studentTotal; i++) {
            ScreeningPlanSchoolStudent planSchoolStudent = new ScreeningPlanSchoolStudent();
            planSchoolStudent.setSrcScreeningNoticeId(plan.getSrcScreeningNoticeId());
            planSchoolStudent.setScreeningTaskId(plan.getScreeningTaskId());
            planSchoolStudent.setScreeningPlanId(plan.getId());
            planSchoolStudent.setScreeningOrgId(plan.getScreeningOrgId());
            planSchoolStudent.setPlanDistrictId(plan.getDistrictId());
            planSchoolStudent.setSchoolDistrictId(school.getDistrictId());
            planSchoolStudent.setSchoolId(school.getId());
            planSchoolStudent.setSchoolName(school.getName());
            planSchoolStudent.setGradeId(schoolGrade.getGradeId());
            planSchoolStudent.setGradeName(schoolGrade.getGradeName());
            planSchoolStudent.setClassId(schoolClass.getClassId());
            planSchoolStudent.setClassName(schoolClass.getClassName());
            planSchoolStudent.setStudentId(mockStudentList.get(i).getId());
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByName(schoolGrade.getGradeName());
            planSchoolStudent.setGradeType(gradeCodeEnum.getType());
            Date date = getDateByGrade(gradeCodeEnum);
            planSchoolStudent.setBirthday(date);
            planSchoolStudent.setGender(GenderEnum.MALE.type);
            planSchoolStudent.setStudentAge(DateUtil.ageOfNow(date));
            planSchoolStudent.setStudentName(mockStudentList.get(i).getName());
            planSchoolStudent.setArtificial(1);
            planSchoolStudent.setScreeningCode(Long.valueOf(mockStudentList.get(i).getName()));
            mockPlanStudentList.add(planSchoolStudent);
        }
        screeningPlanSchoolStudentService.batchUpdateOrSave(mockPlanStudentList);
    }

    /**
     * 获取筛查学生列表
     *
     * @param requestDTO 请求入参
     * @return List<ScreeningPlanSchoolStudent>
     */
    public List<ScreeningPlanSchoolStudent> getListByRequest(PlanStudentRequestDTO requestDTO) {
        return screeningPlanSchoolStudentService.getByEntity(new ScreeningPlanSchoolStudent()
                .setScreeningPlanId(requestDTO.getScreeningPlanId())
                .setScreeningOrgId(requestDTO.getScreeningOrgId())
                .setSchoolId(requestDTO.getSchoolId())
                .setGradeId(requestDTO.getGradeId())
                .setClassId(requestDTO.getClassId()));
    }

    /**
     * 通过班级类型获取生日
     *
     * @param gradeCodeEnum 年级编码枚举类
     * @return 生日
     */
    private Date getDateByGrade(GradeCodeEnum gradeCodeEnum) {
        // 幼儿园
        if (SchoolAge.KINDERGARTEN.code.equals(gradeCodeEnum.getType())) {
            return DateFormatUtil.parse("2017-1-1", DateFormatUtil.FORMAT_ONLY_DATE);
        }
        // 中小学
        return DateFormatUtil.parse("2010-1-1", DateFormatUtil.FORMAT_ONLY_DATE);
    }
}

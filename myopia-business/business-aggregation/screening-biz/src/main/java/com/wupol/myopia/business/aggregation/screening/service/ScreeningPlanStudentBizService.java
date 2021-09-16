package com.wupol.myopia.business.aggregation.screening.service;

import com.wupol.myopia.business.aggregation.screening.domain.dto.UpdatePlanStudentRequestDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author HaoHao
 * @Date 2021/9/16
 **/
@Service
public class ScreeningPlanStudentBizService {

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private StudentService studentService;

    /**
     * 更新筛查学生
     *
     * @param requestDTO 更新学生入参
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePlanStudent(UpdatePlanStudentRequestDTO requestDTO) {
        // 更新计划学生信息
        ScreeningPlanSchoolStudent planSchoolStudent = screeningPlanSchoolStudentService.getById(requestDTO.getPlanStudentId());
        planSchoolStudent.setStudentName(requestDTO.getName());
        planSchoolStudent.setGender(requestDTO.getGender());
        planSchoolStudent.setStudentAge(requestDTO.getStudentAge());
        planSchoolStudent.setBirthday(requestDTO.getBirthday());
        if (StringUtils.isNotBlank(requestDTO.getParentPhone())) {
            planSchoolStudent.setParentPhone(requestDTO.getParentPhone());
        }
        if (StringUtils.isNotBlank(requestDTO.getSno())) {
            planSchoolStudent.setStudentNo(requestDTO.getSno());
        }
        screeningPlanSchoolStudentService.updateById(planSchoolStudent);
        // 更新原始学生信息
        Student student = studentService.getById(planSchoolStudent.getStudentId());
        student.setName(requestDTO.getName());
        student.setGender(requestDTO.getGender());
        student.setBirthday(requestDTO.getBirthday());
        if (StringUtils.isNotBlank(requestDTO.getParentPhone())) {
            student.setParentPhone(requestDTO.getParentPhone());
        }
        if (StringUtils.isNotBlank(requestDTO.getSno())) {
            student.setSno(requestDTO.getSno());
        }
        studentService.updateById(student);
    }
}

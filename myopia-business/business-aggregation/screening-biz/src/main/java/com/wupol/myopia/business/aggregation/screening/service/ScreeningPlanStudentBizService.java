package com.wupol.myopia.business.aggregation.screening.service;

import com.wupol.myopia.business.aggregation.screening.domain.dto.UpdatePlanStudentRequestDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
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

        ScreeningPlanSchoolStudent planSchoolStudent = screeningPlanSchoolStudentService.getById(requestDTO.getPlanStudentId());
        planSchoolStudent.setStudentName(requestDTO.getName());
        planSchoolStudent.setGender(requestDTO.getGender());
        planSchoolStudent.setStudentAge(requestDTO.getStudentAge());
        planSchoolStudent.setParentPhone(requestDTO.getParentPhone());
        planSchoolStudent.setBirthday(requestDTO.getBirthday());
        planSchoolStudent.setStudentNo(requestDTO.getSno());
        screeningPlanSchoolStudentService.updateById(planSchoolStudent);

        Student student = studentService.getById(requestDTO.getStudentId());
        student.setName(requestDTO.getName());
        student.setGender(requestDTO.getGender());
        student.setParentPhone(requestDTO.getParentPhone());
        student.setBirthday(requestDTO.getBirthday());
        student.setSno(requestDTO.getSno());
        studentService.updateById(student);
    }
}

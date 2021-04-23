package com.wupol.myopia.business.aggregation.student.service;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.system.service.ResourceFileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author HaoHao
 * @Date 2021/4/20
 **/
@Service
public class StudentFacade {

    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private StudentService studentService;

    @Autowired
    private SchoolGradeService schoolGradeService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private SchoolClassService schoolClassService;

    /**
     * 更新学生
     *
     * @param student 学生实体类
     * @return 学生实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public StudentDTO updateStudent(Student student) {

        // 设置学龄
        if (null != student.getGradeId()) {
            SchoolGrade grade = schoolGradeService.getById(student.getGradeId());
            student.setGradeType(GradeCodeEnum.getByCode(grade.getGradeCode()).getType());
        }

        // 检查学生身份证是否重复
        if (studentService.checkIdCard(student.getIdCard(), student.getId())) {
            throw new BusinessException("学生身份证重复");
        }

        // 更新学生
        studentService.updateById(student);
        Student resultStudent = studentService.getById(student.getId());
        StudentDTO studentDTO = new StudentDTO();
        BeanUtils.copyProperties(resultStudent, studentDTO);
        if (StringUtils.isNotBlank(studentDTO.getSchoolNo())) {
            School school = schoolService.getBySchoolNo(studentDTO.getSchoolNo());
            studentDTO.setSchoolName(school.getName());
            studentDTO.setSchoolId(school.getId());

            // 查询年级和班级
            SchoolGrade schoolGrade = schoolGradeService.getById(resultStudent.getGradeId());
            SchoolClass schoolClass = schoolClassService.getById(resultStudent.getClassId());
            studentDTO.setGradeName(schoolGrade.getName()).setClassName(schoolClass.getName());
        }
        if (null != resultStudent.getAvatarFileId()) {
            studentDTO.setAvatar(resourceFileService.getResourcePath(resultStudent.getAvatarFileId()));
        }
        studentDTO.setScreeningCount(student.getScreeningCount())
                .setQuestionnaireCount(student.getQuestionnaireCount())
                // TODO: 就诊次数
                .setNumOfVisits(0);
        return studentDTO;
    }


}

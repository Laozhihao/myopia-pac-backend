package com.wupol.myopia.business.screening.domain.vo;

import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import lombok.Getter;

/**
 * @Description
 * @Date 2021/3/1 23:42
 * @Author by Jacob
 */
@Getter
public class StudentVO {

    private Integer studentId;
    /**
     * studentName
     */
    private String studentName;
    /**
     * birthday
     */
    private String birthday;
    /**
     * sex
     */
    private String sex;
    /**
     * schoolName
     */
    private String schoolName;
    /**
     * grade
     */
    private String grade;
    /**
     * clazz
     */
    private String clazz;
    /**
     * deptId
     */
    private Integer deptId;

    private StudentVO() {

    }


    /**
     * 获取实例
     *
     * @param screeningPlanSchoolStudent
     * @return
     */
    public static StudentVO getInstance(ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        StudentVO studentVO = new StudentVO();
        studentVO.studentId = screeningPlanSchoolStudent.getId();
        studentVO.sex = screeningPlanSchoolStudent.getGender()+"";
        studentVO.schoolName = screeningPlanSchoolStudent.getSchoolName();
        studentVO.studentName = screeningPlanSchoolStudent.getStudentName();
        studentVO.grade = screeningPlanSchoolStudent.getGradeName();
        studentVO.clazz = screeningPlanSchoolStudent.getClassName();
        studentVO.birthday = DateFormatUtil.format(screeningPlanSchoolStudent.getBirthDate(),DateFormatUtil.FORMAT_ONLY_DATE);
        studentVO.deptId = screeningPlanSchoolStudent.getScreeningOrgId();
        return studentVO;
    }
}

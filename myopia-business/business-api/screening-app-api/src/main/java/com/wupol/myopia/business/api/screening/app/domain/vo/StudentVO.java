package com.wupol.myopia.business.api.screening.app.domain.vo;

import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import lombok.Getter;

/**
 * @Description
 * @Date 2021/3/1 23:42
 * @Author by Jacob
 */
@Getter
public class StudentVO {

    /**
     * 学生ID
     */
    private String studentId;

    /**
     * 学籍号
     */
    private String studentNo;

    /**
     * 用户名称
     */
    private String studentName;

    /**
     * 出生日期
     */
    private String birthday;

    /**
     * 性别
     */
    private String sex;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 年级
     */
    private String grade;


    private String clazz;


    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 部门ID
     */
    private Integer deptId;

    /**
     * 用户userID
     */
    private Integer userId = 1;


    /**
     *
     */
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
        studentVO.studentId = screeningPlanSchoolStudent.getId().toString();
        studentVO.sex = GenderEnum.getName(screeningPlanSchoolStudent.getGender());
        studentVO.schoolName = screeningPlanSchoolStudent.getSchoolName();
        studentVO.studentNo = screeningPlanSchoolStudent.getStudentNo();
        studentVO.studentName = screeningPlanSchoolStudent.getStudentName();
        studentVO.grade = screeningPlanSchoolStudent.getGradeName();
        studentVO.clazz = screeningPlanSchoolStudent.getClassName();
        studentVO.schoolId = screeningPlanSchoolStudent.getSchoolId();
        studentVO.birthday = DateFormatUtil.format(screeningPlanSchoolStudent.getBirthday(), DateFormatUtil.FORMAT_ONLY_DATE);
        studentVO.deptId = screeningPlanSchoolStudent.getScreeningOrgId();
        return studentVO;
    }
}

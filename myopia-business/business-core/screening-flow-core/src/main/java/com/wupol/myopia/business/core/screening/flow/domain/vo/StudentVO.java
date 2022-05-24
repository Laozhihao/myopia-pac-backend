package com.wupol.myopia.business.core.screening.flow.domain.vo;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * @Description
 * @Date 2021/3/1 23:42
 * @Author by Jacob
 */
@ToString
@Getter
public class StudentVO {

    private StudentVO() {
    }

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
     * 学龄段
     */
    private Integer gradeType;

    /**
     * 筛查编号
     */
    private Long screeningCode;

    /**
     * 筛查计划--参与筛查的学生年级ID
     */
    private Integer gradeId;

    /**
     * 筛查计划--参与筛查的学生班级ID
     */
    private Integer classId;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 获取实例
     *
     * @param screeningPlanSchoolStudent 筛查学生信息
     */
    public static StudentVO getInstance(ScreeningPlanSchoolStudent screeningPlanSchoolStudent) {
        if (Objects.isNull(screeningPlanSchoolStudent)) {
            throw new BusinessException("找不到该筛查学生，请确认!");
        }
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
        studentVO.gradeType = screeningPlanSchoolStudent.getGradeType();
        studentVO.screeningCode = screeningPlanSchoolStudent.getScreeningCode();
        studentVO.gradeId = screeningPlanSchoolStudent.getGradeId();
        studentVO.classId = screeningPlanSchoolStudent.getClassId();
        studentVO.idCard = screeningPlanSchoolStudent.getIdCard();
        return studentVO;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}

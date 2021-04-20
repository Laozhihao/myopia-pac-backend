package com.wupol.myopia.business.screening.others;

import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchoolStudent;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class SysStudent implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 学生ID
     */
    private String studentId;

    /**
     * 微信openId
     */
    private String openId;

    /**
     * 人脸token
     */
    private String faceToken;
    /**
     * 人脸图片
     */
    private String faceImg;

    /**
     * 学籍号
     */
    private String studentNo;

    /**
     * 用户名称
     */
    private String studentName;

    /**
     * 手机号
     */
    private String studentPhone;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区域
     */
    private String region;

    /**
     * 街道
     */
    private String street;

    /**
     * 出生日期
     */
    private String birthday;

    /**
     * 性别
     */
    private String sex;

    /**
     * 民族
     */
    private String clan;

    /**
     * 籍贯
     */
    private String address;

    /**
     * 学校地址
     */
    private String schoolAdress;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 年级
     */
    private String grade;

    /**
     * 班级
     */
    private String clazz;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 学校ID
     */
    private Long schoolId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户userID
     */
    private Long userId;

    /**
     * 学生状态
     */
    private Integer reviewState = 0;

    /**
     * 学生是否毕业
     */
    private Integer isGraduation = 0;


    private SysStudent() {

    }

    public static SysStudent getInstance(ScreeningPlanSchoolStudent screeningPlanSchoolStudent, Integer state) {
        SysStudent sysStudent = new SysStudent();
        sysStudent.studentName = screeningPlanSchoolStudent.getStudentName();
        sysStudent.grade = screeningPlanSchoolStudent.getGradeName();
        sysStudent.schoolName = screeningPlanSchoolStudent.getSchoolName();
        sysStudent.clazz = screeningPlanSchoolStudent.getClassName();
        sysStudent.reviewState = state == null ? 0 : state;
        sysStudent.studentId = screeningPlanSchoolStudent.getId().toString();
        return sysStudent;
    }
}

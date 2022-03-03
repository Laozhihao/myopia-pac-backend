package com.wupol.myopia.business.api.screening.app.domain.dto;

import com.wupol.myopia.base.exception.BusinessException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;


@Data
public class AppStudentDTO {

    /**
     * 学生ID
     */
    private String studentId;

    /**
     * 微信openId
     */
    private String openId;

    /**
     * 学籍号
     */
    private String studentNo;

    /**
     * 人脸token
     */
    private String faceToken;

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
     * 学校ID
     */
    private Long schoolId;

    /**
     * 部门ID
     */
    private Integer deptId;

    /**
     *
     */
    private Long userId;

    /**
     * 护照
     */
    private String passport;

    /**
     * 检查学生信息是否正确
     * <p>
     *     身份证和护照二选一
     * </p>
     */
    public void checkStudentInfo() {
        if (StringUtils.isAllBlank(idCard, passport) || (StringUtils.isNotBlank(idCard) && StringUtils.isNotBlank(passport))) {
            throw new BusinessException("身份证、护照信息异常");
        }
    }

}

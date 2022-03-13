package com.wupol.myopia.business.core.parent.domain.dos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @Author xjl
 * @Date 2022/3/8 16:23
 */
@Data
public class StudentDO {
    /**
     * id
     */
    private Integer id;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 学号
     */
    private String sno;

    /**
     * 年级ID
     */
    private Integer gradeId;

    /**
     * 学龄段
     */
    private Integer gradeType;

    /**
     * 班级id
     */
    private Integer classId;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * 民族 0-汉族
     */
    private Integer nation;

    /**
     * 身份证号码
     */
    private String idCard;

    /**
     * 家长手机号码
     */
    private String parentPhone;

    /**
     * 家长公众号手机号码
     */
    private String mpParentPhone;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 学校ID
     */
    private Integer schoolId;


    /**
     * 护照
     */
    private String passport;

}

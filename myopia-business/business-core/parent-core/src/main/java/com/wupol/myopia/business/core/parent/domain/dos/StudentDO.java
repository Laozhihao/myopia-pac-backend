package com.wupol.myopia.business.core.parent.domain.dos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author xjl
 * @Date 2022/3/8 16:23
 */
@Data
@Accessors(chain = true)
public class StudentDO implements Serializable {
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
     * 年级名称
     */
    private String gradeName;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 学校名称
     */
    private String schoolName;

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
    private String birthday;

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

    /**
     * 筛查编号
     */
    private Long screeningCode;

    /**
     * 筛查日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date screeningDate;

    /**
     * 筛查标题
     */
    private String screeningTitle;

}

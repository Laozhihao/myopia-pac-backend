package com.wupol.myopia.migrate.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 学生表
 *
 * @Author HaoHao
 * @Date 2022-03-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_student")
public class SysStudent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "student_id", type = IdType.AUTO)
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
     * 人脸图片
     */
    private String faceImg;

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
     * 出生日期
     */
    private String birthday;

    /**
     * 性别
     */
    private String sex;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 学校ID
     */
    private String schoolId;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 录入用户
     */
    private String userId;

    /**
     * 是否毕业
     */
    private Integer isGraduation;

    /**
     * 是否删除
     */
    private Integer state;

    /**
     * 疾控编码
     */
    private String studentCode;

    /**
     * 缓存学校
     */
    private String cacheKeySchool;

    /**
     * 学校复测状态
     */
    private Long reviewStateSchool;

    /**
     * 年级复测状态
     */
    private Long reviewStateGrade;

    /**
     * 班级复测状态
     */
    private Long reviewStateClass;

    /**
     * 缓存学校
     */
    private String cacheKeyGrade;

    /**
     * 缓存班级
     */
    private String cacheKeyClass;

    /**
     * 复测状态
     */
    private Long reviewState;

    /**
     * 体格复测状态
     */
    private Long reviewBodyState;

    /**
     * 学校体格复测状态
     */
    private Long reviewBodyStateSchool;

    /**
     * 年级体格复测状态
     */
    private Long reviewBodyStateGrade;

    /**
     * 班级体格复测状态
     */
    private Long reviewBodyStateClass;

    /**
     * 毕业时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date graduationTime;


}

package com.wupol.myopia.business.management.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.base.util.RegularUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

/**
 * 学校-学生表
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_student")
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学校id
     */
    private Integer schoolId;

    /**
     * 根据规则创建ID
     */
    private String studentNo;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 学号
     */
    @NotNull(message = "学号不能为空")
    private Integer sno;

    /**
     * 年级ID
     */
    @NotNull(message = "年级ID不能为空")
    private Integer gradeId;

    /**
     * 班级id
     */
    @NotNull(message = "班级id不能为空")
    private Integer classId;

    /**
     * 学生姓名
     */
    @NotBlank(message = "学生姓名不能为空")
    private String name;

    /**
     * 性别 1-男 2-女
     */
    @NotNull(message = "性别不能为空")
    private Integer gender;

    /**
     * 出生日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "出生日期不能为空")
    private Date birthday;

    /**
     * 民族 0-汉族
     */
    @NotNull(message = "学号不能为空")
    private Integer nation;

    /**
     * 身份证号码
     */
    @Pattern(regexp = RegularUtils.REGULAR_ID_CARD, message = "身份证格式错误")
    @NotNull(message = "学号不能为空")
    private String idCard;

    /**
     * 家长手机号码
     */
    @Pattern(regexp = RegularUtils.REGULAR_MOBILE, message = "手机号码格式错误")
    @NotNull(message = "学号不能为空")
    private String parentPhone;

    /**
     * 家长公众号手机号码
     */
    private String mpParentPhone;

    /**
     * 省代码
     */
    @NotNull(message = "省代码不能为空")
    private Integer provinceCode;

    /**
     * 市代码
     */
    @NotNull(message = "市代码不能为空")
    private Integer cityCode;

    /**
     * 区代码
     */
    @NotNull(message = "区代码不能为空")
    private Integer areaCode;

    /**
     * 镇/乡代码
     */
    @NotNull(message = "镇/乡代码不能为空")
    private Integer townCode;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    private String address;

    /**
     * 当前情况
     */
    private String currentSituation;

    /**
     * 视力标签
     */
    private String labels;

    /**
     * 视力筛查次数
     */
    private Integer screeningCount;

    /**
     * 问卷数
     */
    private Integer questionnaireCount;

    /**
     * 最近筛选次数
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastScreeningTime;

    /**
     * 状态 0-启用 1-禁止 2-删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;


}

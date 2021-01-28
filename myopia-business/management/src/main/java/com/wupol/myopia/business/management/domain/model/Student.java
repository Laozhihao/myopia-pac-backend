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
     * 学校编码
     */
    private String schoolNo;

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
    private Integer gradeId;

    /**
     * 班级id
     */
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
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @NotNull(message = "出生日期不能为空")
    private Date birthday;

    /**
     * 民族 0-汉族
     */
    private Integer nation;

    /**
     * 身份证号码
     */
    @Pattern(regexp = RegularUtils.REGULAR_ID_CARD, message = "身份证格式错误")
    @NotNull(message = "身份证号码不能为空")
    private String idCard;

    /**
     * 家长手机号码
     */
    @Pattern(regexp = RegularUtils.REGULAR_MOBILE, message = "手机号码格式错误")
    private String parentPhone;

    /**
     * 家长公众号手机号码
     */
    private String mpParentPhone;

    /**
     * 省代码
     */
    private Long provinceCode;

    /**
     * 市代码
     */
    private Long cityCode;

    /**
     * 区代码
     */
    private Long areaCode;

    /**
     * 镇/乡代码
     */
    private Long townCode;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 当前情况
     */
    private String currentSituation;

    /**
     * 视力标签 0-零级、1-一级、2-二级、3-三级
     */
    private Integer visionLabel;

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

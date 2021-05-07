package com.wupol.myopia.business.core.hospital.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.base.util.RegularUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

/**
 * 医院-学生
 * @author Chikong
 * @date 2021-02-10
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@TableName("h_hospital_student")
public class HospitalStudent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 等同学生id */
    private Integer id;

    /** 医院id */
    private Integer hospitalId;

    /** 学校id */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer schoolId;

    /** 年级id */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer gradeId;

    /** 班级id */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer classId;

    /** 学生姓名 */
    @NotBlank(message = "学生姓名不能为空")
    private String name;

    /** 性别 0-男 1-女 */
    @Range(min = 0, max = 1)
    private Integer gender;

    /** 出生日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @NotNull(message = "出生日期不能为空")
    private Date birthday;

    /** 民族 0-汉族 */
    private Integer nation;

    /** 身份证号码 */
    @Pattern(regexp = RegularUtils.REGULAR_ID_CARD, message = "身份证格式错误")
    @NotNull(message = "身份证号码不能为空")
    private String idCard;

    /** 家长手机号码 */
    private String parentPhone;

    /** 家长公众号手机号码 */
    private String mpParentPhone;

    /** 省代码 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer provinceId;

    /** 市代码 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer cityId;

    /** 区代码 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer areaId;

    /** 镇/乡代码 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer townId;

    /** 详细地址 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String address;

    /** 状态 0-启用 1-禁止 2-删除 */
    private Integer status;

    /** 创建时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;


    public HospitalStudent(Integer hospitalId, Integer id) {
        this.id = id;
        this.hospitalId = hospitalId;
    }

}

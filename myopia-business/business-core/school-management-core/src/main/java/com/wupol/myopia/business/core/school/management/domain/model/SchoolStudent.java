package com.wupol.myopia.business.core.school.management.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.core.common.domain.model.AddressCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 学校-学生表
 *
 * @author Simple4H
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "m_school_student")
public class SchoolStudent extends AddressCode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学生Id
     */
    private Integer studentId;
    
    /**
     * 学校Id
     */
    @NotNull(message = "学校Id不能为空")
    private Integer schoolId;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 学号
     */
    @NotBlank(message = "学籍号不能为空")
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
     * 学龄段
     */
    private Integer gradeType;

    /**
     * 班级id
     */
    private Integer classId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 学生姓名
     */
    @NotBlank(message = "学生姓名不能为空")
    private String name;

    /**
     * 性别 0-男 1-女
     */
    @Range(min = 0, max = 1)
    private Integer gender;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "出生日期不能为空")
    private Date birthday;

    /**
     * 民族 0-汉族
     */
    private Integer nation;

    /**
     * 身份证号码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
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
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String address;

    /**
     * 状态 0-启用 1-禁止 2-删除
     */
    private Integer status;

    /**
     * 戴镜类型
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer glassesType;

    /**
     * 最近筛选时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastScreeningTime;

    /**
     * 视力标签 0-零级、1-一级、2-二级、3-三级
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer visionLabel;

    /**
     * 视力低下等级, 1-视力低下 （暂时只显示这个）
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer lowVision;

    /**
     * 近视等级，0-正常、1-筛查性近视、2-近视前期、3-低度近视、4-中度近视、5-重度近视
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer myopiaLevel;

    /**
     * 筛查性近视
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer screeningMyopia;

    /**
     * 远视等级，0-正常、1-远视、2-低度远视、3-中度远视、4-重度远视
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer hyperopiaLevel;

    /**
     * 散光等级，0-正常、1-低度散光、2-中度散光、3-重度散光
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer astigmatismLevel;

    /**
     * 护照
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String passport;

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
     * 学生来源客户端
     */
    private Integer sourceClient;

    /**
     *  是否屈光参差
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isAnisometropia;

    /** 是否屈光不正 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isRefractiveError;

    /** 视力矫正状态 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer visionCorrection;

    /**
     * 是否近视
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isMyopia;

    /**
     * 是否远视
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isHyperopia;

    /**
     * 是否散光
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isAstigmatism;

    /**
     * 入学年份
     */
    private Integer particularYear;

    /**
     * 检查学生信息是否正确
     * <p>
     *     身份证和护照二选一
     * </p>
     */
    public void checkStudentInfo() {
        if (StringUtils.isAllBlank(idCard, passport) || (StringUtils.isNotBlank(idCard) && StringUtils.isNotBlank(passport))) {
            throw new BusinessException("身份证或护照不能为空，且二选一");
        }
    }

}

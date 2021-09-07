package com.wupol.myopia.business.core.screening.flow.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.base.util.RegularUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

/**
 * 参与筛查计划的学生表
 *
 * @author Alix
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_screening_plan_school_student")
public class ScreeningPlanSchoolStudent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 筛查计划--所属的筛查源通知id（也即task的来源通知id），自己创建时默认0
     */
    private Integer srcScreeningNoticeId;

    /**
     * 筛查计划--所属的筛查任务id
     */
    private Integer screeningTaskId;

    /**
     * 筛查计划--计划id
     */
    @NotNull(message = "筛查计划ID不能为空")
    private Integer screeningPlanId;

    /**
     * 筛查计划--指定的筛查机构id
     */
    private Integer screeningOrgId;

    /**
     * 筛查计划--所处区域id
     */
    private Integer planDistrictId;

    /**
     * 筛查计划--学生学校所处区域id
     */
    private Integer schoolDistrictId;

    /**
     * 筛查计划--执行的学校id
     */
    @NotNull(message = "筛查学校ID不能为空")
    private Integer schoolId;

    /**
     * 筛查计划--执行的学校编号
     */
    private String schoolNo;

    /**
     * 筛查计划--执行的学校名字
     */
    private String schoolName;

    /**
     * 筛查计划--参与筛查的学生年级ID
     */
    @NotNull(message = "筛查年级ID不能为空")
    private Integer gradeId;

    /**
     * 筛查计划--年级名称
     */
    private String gradeName;

    /**
     * 学龄段
     */
    private Integer gradeType;

    /**
     * 筛查计划--参与筛查的学生班级ID
     */
    @NotNull(message = "筛查班级ID不能为空")
    private Integer classId;

    /**
     * 筛查计划--年级名称
     */
    private String className;

    /**
     * 筛查计划--参与筛查的学生id
     */
    private Integer studentId;

    /**
     * 筛查计划--参与筛查的学生身份证号码
     */
    @Pattern(regexp = RegularUtils.REGULAR_ID_CARD, message = "身份证格式错误")
    private String idCard;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 筛查计划--参与筛查的学生年龄
     */
    private Integer studentAge;

    /**
     * 筛查计划--参与筛查的当时情况
     */
    private String studentSituation;

    /**
     * 筛查计划--参与筛查的学生编号
     */
    private String studentNo;

    /**
     * 筛查计划--参与筛查的学生名字
     */
    private String studentName;

    /**
     * 筛查计划--创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 省代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long provinceCode;

    /**
     * 市代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long cityCode;

    /**
     * 区代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long areaCode;

    /**
     * 镇/乡代码
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long townCode;

    /**
     * 详细地址
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String address;

    /**
     * 家长手机号码
     */
    private String parentPhone;

    /**
     * 民族 0-汉族
     */
    private Integer nation;

    /**
     * 0-非人造的、1-人造的
     */
    private Integer artificial;

    /**
     * 筛查编号
     */
    private Long screeningCode;


}

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
     * 筛查计划--所属的筛查任务id
     */
    private Integer screeningTaskId;

    /**
     * 筛查计划--计划id
     */
    @NotNull(message = "筛查计划ID不能为空")
    private Integer screeningPlanId;

    /**
     * 地区id
     */
    private Integer districtId;

    /**
     * 筛查任务id
     */
    private Integer screeningTaskId;

    /**
     * 原始的通知id
     */
    private Integer srcScreeningNoticeId;

    /**
     * 筛查机构id
     */
    private Integer screeningOrgId;

    /**
     * 筛查计划--指定的筛查机构id
     */
    private Integer screeningOrgId;

    /**
     * 筛查计划--所处区域id
     */
    private Integer districtId;

    /**
     * 筛查计划--执行的学校id
     */
    @NotNull(message = "筛查学校ID不能为空")
    private Integer schoolId;

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
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * 性别 1-男 2-女
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
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}

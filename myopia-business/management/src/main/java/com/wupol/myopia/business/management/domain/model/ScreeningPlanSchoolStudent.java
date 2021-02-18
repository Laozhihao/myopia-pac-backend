package com.wupol.myopia.business.management.domain.model;

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
 * 参与筛查计划的学生表
 *
 * @Author HaoHao
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
     * 筛查计划--计划id 
     */
    private Integer screeningPlanId;

    /**
     * 筛查计划--执行的学校id
     */
    private Integer schoolId;

    /**
     * 筛查计划--执行的学校名字
     */
    private String schoolName;

    /**
     * 筛查计划--参与筛查的学生年级ID
     */
    private Integer gradeId;

    /**
     * 筛查计划--参与筛查的学生班级ID
     */
    private Integer classId;

    /**
     * 筛查计划--参与筛查的学生id
     */
    private Integer studentId;

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

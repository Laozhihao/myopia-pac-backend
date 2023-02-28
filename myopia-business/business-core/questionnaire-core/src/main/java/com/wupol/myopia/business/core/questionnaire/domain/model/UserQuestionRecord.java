package com.wupol.myopia.business.core.questionnaire.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户答问卷记录表
 *
 * @Author Simple4H
 * @Date 2022-07-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("q_user_question_record")
public class UserQuestionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户Id
     */
    private Integer userId;

    /**
     * 用户类型 0-学生 1-学校
     */
    private Integer userType;

    /**
     * 问卷Id
     */
    private Integer questionnaireId;

    /**
     * 计划Id
     */
    private Integer planId;

    /**
     * 任务Id
     */
    private Integer taskId;

    /**
     * 政府Id
     */
    private Integer govId;

    /**
     * 通知Id
     */
    private Integer noticeId;

    /**
     * 学校Id
     */
    private Integer schoolId;

    /**
     * 学生Id
     */
    private Integer studentId;

    /**
     * 区域Code
     */
    private Long districtCode;

    /**
     * 问卷类型
     */
    private Integer questionnaireType;

    /**
     * 状态 0-未开始 1-进行中 2-结束
     */
    private Integer status;

    /**
     * 汇总类型 1-汇总
     */
    private Integer recordType;

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
}

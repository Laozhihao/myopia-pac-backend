package com.wupol.myopia.business.core.questionnaire.domain.model;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 用户答案进度表
 *
 * @Author Simple4H
 * @Date 2022-07-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("q_user_answer_progress")
public class UserAnswerProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 区域Id
     */
    private Integer districtId;

    /**
     * 学校Id
     */
    private Integer schoolId;

    /**
     * currentStep
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String currentStep;

    /**
     * currentSideBar
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String currentSideBar;

    /**
     * 步骤JSON
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String stepJson;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date updateTime;


}

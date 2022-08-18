package com.wupol.myopia.business.core.questionnaire.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.questionnaire.domain.dos.OptionAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.handle.OptionAnswerHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "q_user_answer", autoResultMap = true)
public class UserAnswer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 问卷ID
     */
    private Integer questionnaireId;

    /**
     * 问题ID
     */
    private Integer questionId;

    /**
     * 记录表Id
     */
    private Integer recordId;

    /**
     * 用户类型 0-学生 1-学校
     */
    private Integer userType;

    /**
     * 问题标题
     */
    private String questionTitle;

    /**
     * 用户答案
     */
    @TableField(typeHandler = OptionAnswerHandler.class)
    private List<OptionAnswer> answer;

    /**
     * 表格json
     */
    private String tableJson;

    /**
     * 类型
     */
    private String type;

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

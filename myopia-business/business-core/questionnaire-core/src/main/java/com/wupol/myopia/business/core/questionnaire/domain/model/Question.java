package com.wupol.myopia.business.core.questionnaire.domain.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wupol.myopia.business.core.questionnaire.domain.dto.QuestionAttribute;
import com.wupol.myopia.business.core.questionnaire.domain.dto.Option;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 
 *
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("q_question")
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 问题类型，如：radio（单选）、checkbox（多选）、input（填空）
     */
    private String type;

    /**
     * 问题题目
     */
    private String title;

    /**
     * 问题属性
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QuestionAttribute attribute;

    /**
     * 问题的答案选项
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Option> options;

    /**
     * 问题的序号
     */
    private String serialNumber;

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

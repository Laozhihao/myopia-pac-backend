package com.wupol.myopia.business.core.questionnaire.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
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
@TableName(value = "q_questionnaire_question", autoResultMap = true)
public class QuestionnaireQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 问卷ID
     */
    private Integer questionnaireId;

    /**
     * 题目ID
     */
    private Integer questionId;

    /**
     * 父题目Id，没有父题目的则为-1
     */
    private Integer pid;

    /**
     * 自定义问题的序号
     */
    private String serialNumber;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 跳转题目
     */
    private String jumpIds;

    /**
     * 是否必填
     */
    private Boolean required;


}

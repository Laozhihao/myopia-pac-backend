package com.wupol.myopia.business.core.questionnaire.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wupol.myopia.business.core.questionnaire.domain.dos.JumpIdsDO;
import com.wupol.myopia.business.core.questionnaire.domain.handle.JumpIdsDoHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
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
     * 顶层父级的标志Id
     */
    public static final int TOP_PARENT_ID = -1;

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
     * 是否逻辑题
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Boolean isLogic;

    /**
     * 是否展示题目序号
     */
    private Boolean isShowNumber;

    /**
     * 跳转题目
     */
    @TableField(typeHandler = JumpIdsDoHandler.class, updateStrategy = FieldStrategy.IGNORED)
    private List<JumpIdsDO> jumpIds;

    /**
     * 是否必填
     */
    private Boolean required;

}

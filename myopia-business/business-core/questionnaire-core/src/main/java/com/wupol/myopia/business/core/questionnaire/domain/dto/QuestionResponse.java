package com.wupol.myopia.business.core.questionnaire.domain.dto;

import com.wupol.myopia.business.core.questionnaire.domain.dos.JumpIdsDO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname QuestionResponse
 * @Description
 * @Date 2022/7/11 14:38
 * @Created by limy
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionResponse extends Question implements Serializable {

    private static final long serialVersionUID = 3390658266197220844L;
    /**
     * 是否必填
     */
    private Boolean required;

    /**
     * 扩展id（中间表）
     */
    private Integer exId;

    /**
     * 扩展Pid（中间表）
     */
    private Integer exPid;

    /**
     * 是否不展示题目序号
     */
    private Boolean isNotShowNumber;

    /**
     * 是否逻辑题
     */
    private Boolean isLogic;

    /**
     * 跳转Id
     */
    private List<JumpIdsDO> jumpIds;


    private List<QuestionResponse> questionList;
}

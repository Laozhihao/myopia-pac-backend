package com.wupol.myopia.business.core.questionnaire.domain.dto;

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


    private List<QuestionResponse> questionList;
}

package com.wupol.myopia.business.core.questionnaire.domain.dto;

import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname QuestionResponse
 * @Description
 * @Date 2022/7/11 14:38
 * @Created by limy
 */
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

    /**
     * 是否展示题目序号
     */
    private Boolean isShowNumber;


    private List<QuestionResponse> questionList;
}

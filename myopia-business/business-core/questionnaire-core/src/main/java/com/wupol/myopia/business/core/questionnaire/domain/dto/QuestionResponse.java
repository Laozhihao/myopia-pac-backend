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

    private static final long serialVersionUID = 3390658266197220844L;
    /**
     * 是否必填
     */
    private Boolean required;


    private List<QuestionResponse> questionList;
}

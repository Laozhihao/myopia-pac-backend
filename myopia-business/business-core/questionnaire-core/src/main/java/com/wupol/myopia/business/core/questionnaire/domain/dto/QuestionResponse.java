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


    private List<QuestionResponse> children;
}

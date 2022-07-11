package com.wupol.myopia.business.core.questionnaire.domain.dto;

import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname QuestionnaireInfoDTO
 * @Description
 * @Date 2022/7/8 11:30
 * @Created by limy
 */
@Data
@Builder
public class QuestionnaireInfoDTO implements Serializable {

    /**
     * 父模块名称
     */
    private String partName;

    /**
     * 父模块Id
     */
    private Integer partId;

    /**
     * 子模块问题数组
     */
    private List<Question> questionList;
}

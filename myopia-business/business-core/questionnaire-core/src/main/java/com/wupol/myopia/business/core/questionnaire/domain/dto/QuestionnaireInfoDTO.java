package com.wupol.myopia.business.core.questionnaire.domain.dto;

import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@AllArgsConstructor
@NoArgsConstructor
public class QuestionnaireInfoDTO extends Question implements Serializable {
    /**
     * 子模块问题数组
     */
    private List<QuestionResponse> questionList;
}

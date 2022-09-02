package com.wupol.myopia.business.core.questionnaire.domain.dos;

import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 问卷问题rec数据结构
 *
 * @author hang.yuan 2022/8/21 15:19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireQuestionDataBO {

    /**
     * 问题
     */
    private Question question;

    /**
     * 是否隐藏
     */
    private Boolean isHidden;

    /**
     * 问题对应选项信息
     */
    private List<QuestionnaireDataBO> questionnaireDataBOList;

}

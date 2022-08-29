package com.wupol.myopia.business.aggregation.export.excel.domain.bo;

import com.wupol.myopia.business.core.questionnaire.domain.dos.HideQuestionRecDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 获取用户问卷答案条件
 *
 * @author hang.yuan 2022/8/27 14:09
 */
@Data
@Accessors(chain = true)
public class UserQuestionnaireAnswerCondition {
    private List<UserQuestionRecord> userQuestionRecordList;
    private List<HideQuestionRecDataBO> hideQuestionDataBOList;
    private GenerateDataCondition generateDataCondition;
}

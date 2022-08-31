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
    /**
     * 用户问卷记录集合
     */
    private List<UserQuestionRecord> userQuestionRecordList;
    /**
     * 隐藏问题rec数据集合
     */
    private List<HideQuestionRecDataBO> hideQuestionDataBOList;
    /**
     * 生成数据条件
     */
    private GenerateDataCondition generateDataCondition;
}

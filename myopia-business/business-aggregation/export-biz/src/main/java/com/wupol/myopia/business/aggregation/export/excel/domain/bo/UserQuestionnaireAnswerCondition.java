package com.wupol.myopia.business.aggregation.export.excel.domain.bo;

import com.wupol.myopia.business.core.questionnaire.domain.dos.HideQuestionRecDataBO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionnaireQuestionRecDataBO;
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
     * 问卷问题rec数据结构集合
     */
    private List<QuestionnaireQuestionRecDataBO> dataBuildList;
    /**
     * qes字段集合
     */
    private List<String> qesFieldList;

    /**
     * 最新问卷ID集合
     */
    private List<Integer> latestQuestionnaireIds;
    /**
     * qes文件地址
     */
    private String qesUrl;
}

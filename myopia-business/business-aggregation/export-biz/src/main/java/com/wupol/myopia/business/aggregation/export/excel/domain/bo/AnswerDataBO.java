package com.wupol.myopia.business.aggregation.export.excel.domain.bo;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 答案数据实体
 *
 * @author hang.yuan 2022/8/25 10:52
 */
@Data
@Accessors(chain = true)
public class AnswerDataBO {
    /**
     * 用户问卷记录集合
     */
    private List<UserQuestionRecord> userQuestionRecordList;
    /**
     * 年级类型集合
     */
    private List<Integer> gradeTypeList;
    /**
     * 导出条件对象
     */
    private ExportCondition exportCondition;
    /**
     * 问卷类型
     */
    private QuestionnaireTypeEnum questionnaireTypeEnum;
}

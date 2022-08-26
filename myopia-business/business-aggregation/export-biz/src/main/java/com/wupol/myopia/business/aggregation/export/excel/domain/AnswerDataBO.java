package com.wupol.myopia.business.aggregation.export.excel.domain;

import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
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
    private List<UserQuestionRecord> userQuestionRecordList;
    private List<Integer> gradeTypeList;
    private ExportCondition exportCondition;
}

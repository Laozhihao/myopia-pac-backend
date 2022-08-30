package com.wupol.myopia.business.aggregation.export.excel.domain.bo;

import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 过滤数据条件
 *
 * @author hang.yuan 2022/8/30 16:29
 */
@Data
@Accessors(chain = true)
public class FilterDataCondition {

    private List<UserQuestionRecord> userQuestionRecordList;
    private QuestionnaireTypeEnum questionnaireTypeEnum;
    private Integer districtId;
}

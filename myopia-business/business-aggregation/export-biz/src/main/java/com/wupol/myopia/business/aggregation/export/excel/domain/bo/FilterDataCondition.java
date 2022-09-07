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

    /**
     * 用户问卷记录集合
     */
    private List<UserQuestionRecord> userQuestionRecordList;
    /**
     * 问卷类型
     */
    private QuestionnaireTypeEnum questionnaireTypeEnum;
    /**
     * 地区ID
     */
    private Integer districtId;
}

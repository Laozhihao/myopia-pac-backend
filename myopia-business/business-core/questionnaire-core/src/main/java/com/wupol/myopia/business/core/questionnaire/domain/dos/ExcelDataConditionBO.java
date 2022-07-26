package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 获取excel数据条件实体
 *
 * @author hang.yuan 2022/7/21 19:53
 */
@Data
@Accessors(chain = true)
public class ExcelDataConditionBO {

    /**
     *  筛查计划ID
     */
    private Integer screeningPlanId;

    /**
     * 问卷类型
     */
    private List<Integer> questionnaireTypeList;

    /**
     * 问卷ID
     */
    private Integer questionnaireId;

}

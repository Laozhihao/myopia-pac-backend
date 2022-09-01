package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Data;

/**
 * 问卷excel数据信息
 *
 * @author hang.yuan 2022/9/1 10:23
 */
@Data
public class QuestionnaireExcelDataBO {
    /**
     * qes字段
     */
    private String qesField;

    /**
     * excel答案
     */
    private String excelAnswer;

    /**
     * 选项Id
     */
    private String optionId;

    /**
     * 问题ID
     */
    private Integer questionId;
}

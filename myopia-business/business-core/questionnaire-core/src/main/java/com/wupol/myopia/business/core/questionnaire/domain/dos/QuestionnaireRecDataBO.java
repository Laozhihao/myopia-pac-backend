package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 问卷Rec数据信息
 *
 * @author hang.yuan 2022/8/19 17:03
 */
@Data
@Accessors(chain = true)
public class QuestionnaireRecDataBO {

    /**
     * qes字段
     */
    private String qesField;

    /**
     * rec答案
     */
    private String recAnswer;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 是否必填
     */
    private Boolean required;

    /**
     * 选项Id
     */
    private String optionId;

    /**
     * 是否隐藏
     */
    private Boolean isHidden;


    private List<QuestionnaireRecDataBO> questionnaireRecDataBOList;

}

package com.wupol.myopia.business.api.questionnaire.domain.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 问卷qes响应实体
 * @author hang.yuan
 * @date 2022/8/5
 */
@Data
public class QuestionnaireQesListVO {

    /**
     * 年份
     */
    private List<Integer> yearList;

    /**
     * 年份对应的数据
     */
    private Map<Integer,List<QuestionnaireQesVO>> dataMap;
}

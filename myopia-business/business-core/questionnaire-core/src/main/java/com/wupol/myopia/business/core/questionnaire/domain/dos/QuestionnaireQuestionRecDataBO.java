package com.wupol.myopia.business.core.questionnaire.domain.dos;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 问卷问题rec数据结构
 *
 * @author hang.yuan 2022/8/21 15:19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireQuestionRecDataBO {

    /**
     * 问题
     */
    private Question question;

    /**
     * 是否隐藏
     */
    private Boolean isHidden;

    /**
     * 问题对应选项信息
     */
    private List<QuestionnaireRecDataBO> questionnaireRecDataBOList;


    private Map<String,List<QuestionnaireRecDataBO>> qesDataMap;

    /**
     * 根据qes字段获取对应数据结构
     * @param qesField qes字段
     */
    public List<QuestionnaireRecDataBO> getQesDataMap(String qesField){
        if (CollUtil.isEmpty(questionnaireRecDataBOList)){
            return Lists.newArrayList();
        }

        if (CollUtil.isEmpty(qesDataMap)){
            setQesDataMap(questionnaireRecDataBOList.stream().collect(Collectors.groupingBy(QuestionnaireRecDataBO::getQesField, ConcurrentHashMap::new,Collectors.toList())));
        }
        return qesDataMap.get(qesField);
    }


}

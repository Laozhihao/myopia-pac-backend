package com.wupol.myopia.business.aggregation.export.excel.domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.core.questionnaire.domain.dos.OptionAnswer;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesFieldDataBO;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户问卷答案
 *
 * @author hang.yuan 2022/8/20 17:06
 */
@Data
public class UserQuestionnaireAnswerBO {

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 用户类型 0-学生 1-学校
     */
    private Integer userType;

    /**
     * 学生ID
     */
    private Integer studentId;

    /**
     * 学校Id
     */
    private Integer schoolId;

    /**
     * 用户答案(问题对应答案)
     */
    private Map<Integer, List<OptionAnswer>> questionAnswerMap;

    /**
     * 隐藏问题答案
     */
    private List<QesFieldDataBO> qesFieldDataBOList;


    /**
     * 用户key
     */
    public String getUserKey() {
        return userId + StrUtil.UNDERLINE + userType;
    }

    /**
     * 问题对应答案集合
     */
    public Map<Integer, Map<String, OptionAnswer>> getAnswerMap() {
        if (CollUtil.isEmpty(questionAnswerMap)) {
            return Maps.newHashMap();
        }
        Map<Integer, Map<String, OptionAnswer>> map = Maps.newHashMap();
        questionAnswerMap.forEach((questionId, list) -> map.put(questionId, list.stream().collect(Collectors.toMap(OptionAnswer::getOptionId, Function.identity()))));
        return map;
    }

}

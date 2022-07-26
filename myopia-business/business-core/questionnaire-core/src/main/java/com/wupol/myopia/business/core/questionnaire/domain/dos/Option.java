package com.wupol.myopia.business.core.questionnaire.domain.dos;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 问卷-属性
 *
 * @author Simple4H
 */
@Getter
@Setter
public class Option {

    /**
     * id
     */
    private String id;

    /**
     * 标题
     */
    private String text;

    /**
     * 类型
     */
    private String type;

    /**
     * 属性
     */
    private OptionAttribute attribute;

    /**
     * 系统序号
     */
    private String serialNumber;

    /**
     * 跳转题目Id
     */
    private List<Integer> jumpIds;


    /**
     * 转换里层的json
     */
    private JSONObject option;
}

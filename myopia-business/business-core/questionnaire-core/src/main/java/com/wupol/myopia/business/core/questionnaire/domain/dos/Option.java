package com.wupol.myopia.business.core.questionnaire.domain.dos;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 问卷-属性
 *
 * @author Simple4H
 */
@Data
public class Option implements Serializable {

    private static final long serialVersionUID = 8172911895182556183L;
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
    private List<JumpIdsDO.JumpIdItem> jumpIds;

    /**
     * 记分题目-分值
     */
    private Integer scoreValue;

    /**
     * 转换里层的json
     */
    private JSONObject option;

//    /**
//     * qes序号
//     */
//    private String qesSerialNumber;
//
//    /**
//     * 展示序号
//     */
//    private String showSerialNumber;
//
//    /**
//     * qes字段
//     */
//    private String qesField;
}

package com.wupol.myopia.business.core.questionnaire.domain.dto;

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
public class Options {

    /**
     * id
     */
    private Integer id;

    /**
     * 数据类型
     */
    private Integer dataType;

    /**
     * 序号
     */
    private String serialNumber;

    /**
     * 系统序号
     */
    private String systemSerialNumber;

    /**
     * 答案选项
     */
    private String answer;

    /**
     * 是否完成
     */
    private String complete;

    /**
     * 标题
     */
    private String text;

    /**
     * 是否互斥
     */
    private Boolean exclusive;

    /**
     * 逻辑题
     */
    private List<Object> placeholderSetting;
}

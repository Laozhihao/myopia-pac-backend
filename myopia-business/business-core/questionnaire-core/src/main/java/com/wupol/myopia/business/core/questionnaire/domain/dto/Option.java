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
}

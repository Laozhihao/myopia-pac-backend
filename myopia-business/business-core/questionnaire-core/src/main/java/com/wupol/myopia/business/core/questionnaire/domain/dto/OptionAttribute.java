package com.wupol.myopia.business.core.questionnaire.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 选项属性
 *
 * @author Simple4H
 */
@Getter
@Setter
public class OptionAttribute {

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 是否互斥
     */
    private Boolean exclusive;

    /**
     * 文本长度
     */
    private Integer length;

    /**
     * 数值最小限制
     */
    private Integer minLimit;

    /**
     * 数值最大限制
     */
    private Integer maxLimit;

    /**
     * 是否必填
     */
    private Boolean required;


}

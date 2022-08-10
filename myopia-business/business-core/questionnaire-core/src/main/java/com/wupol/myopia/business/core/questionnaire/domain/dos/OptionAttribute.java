package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 选项属性
 *
 * @author Simple4H
 */
@Getter
@Setter
public class OptionAttribute implements Serializable {

    private static final long serialVersionUID = 6938795276392009301L;
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

    /**
     * 小数位
     */
    private Integer range;


}

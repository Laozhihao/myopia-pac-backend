package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Data;

/**
 * Input选项实体
 *
 * @author hang.yuan
 * @date 2022/8/19
 */
@Data
public class InputOption {
    /**
     * 选项ID
     */
    private String id;
    /**
     * 范围
     */
    private Integer range;
    /**
     * 数据文本
     */
    private String dataText;
    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 最大限制
     */
    private Integer maxLimit;
    /**
     * 最小限制
     */
    private Integer minLimit;
    /**
     * 是否必填
     */
    private Boolean required;
    /**
     * 是否互斥
     */
    private Boolean exclusive;
    /**
     * 长度
     */
    private Integer length;
}
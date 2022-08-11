package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 表格详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class TableItem implements Serializable {

    /**
     * id
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;

    /**
     * 下拉key
     */
    private String dropSelectKey;

    /**
     * 问题Id
     */
    private Integer questionId;

    /**
     * 是否必填
     */
    private Boolean required;
}


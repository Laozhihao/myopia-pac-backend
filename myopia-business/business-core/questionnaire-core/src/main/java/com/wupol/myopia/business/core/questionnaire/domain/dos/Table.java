package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class Table implements Serializable {

    /**
     * 表格项
     */
    private List<TableItem> item;
}

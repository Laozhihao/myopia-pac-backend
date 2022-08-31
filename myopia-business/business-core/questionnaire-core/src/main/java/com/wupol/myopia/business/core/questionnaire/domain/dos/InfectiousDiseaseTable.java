package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 传染病表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class InfectiousDiseaseTable implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * 表格
     */
    private List<Detail> tableItems;

    @Getter
    @Setter
    public static class Detail implements Serializable{
        /**
         * 表格项
         */
        private List<TableItem> tableItems;
    }

}

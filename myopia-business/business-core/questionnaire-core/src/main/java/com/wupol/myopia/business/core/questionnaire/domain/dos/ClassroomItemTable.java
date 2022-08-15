package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 教室表格
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ClassroomItemTable implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * 详情
     */
    private List<Detail> tableItems;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Detail implements Serializable{

        /**
         * 表格项
         */
        private List<TableItem> tableItems;
    }

}

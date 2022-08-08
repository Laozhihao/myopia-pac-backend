package com.wupol.myopia.business.core.questionnaire.domain.dos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 学校教师
 *
 * @author Simple4H
 */
public class SchoolTeacherTable implements Serializable{

    /**
     * 名称
     */
    private String name;

    /**
     * 详情
     */
    private List<ClassroomItemTable.Detail> details;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Detail implements Serializable {

        /**
         * 表格项
         */
        private List<TableItem> info;
    }
}

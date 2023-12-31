package com.wupol.myopia.business.core.questionnaire.domain.dos;

import com.wupol.myopia.business.core.questionnaire.constant.QuestionnaireConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 表格详情
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    /**
     * 前端标识
     */
    private Integer frontMark;

    /**
     * 构建成前端需要的option
     */
    private Option option;

    public TableItem(String name) {
        this.name = name;
        this.type = QuestionnaireConstant.TEXT;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option implements Serializable {

        private Integer maxLimit;

        private Integer minLimit;

        private Integer length;

        private Integer range;

        /**
         * 数据类型
         */
        private String dataType;

        /**
         * 是否必填写
         */
        private Boolean required;

    }
}


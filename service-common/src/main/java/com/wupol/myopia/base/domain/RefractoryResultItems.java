package com.wupol.myopia.base.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 验光仪检查结果
 *
 * @author Simple4H
 */
@Getter
@Setter
public class RefractoryResultItems {

    /**
     * 标题
     */
    private String title;

    /**
     * 右眼
     */
    private Item od;

    /**
     * 左眼
     */
    private Item os;

    public RefractoryResultItems(String title) {
        this.title = title;
    }

    public RefractoryResultItems() {
    }

    @Getter
    @Setter
    public static class Item {
        /**
         * 视力
         */
        private BigDecimal vision;

        /**
         * 学生筛查报告常量
         */
        private Integer type;

        /**
         * 类型名称
         */
        private String typeName;
    }
}

package com.wupol.myopia.business.parent.domain.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

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

    @Getter
    @Setter
    public static class Item {
        /**
         * 视力
         */
        private String vision;

        /**
         * 类型
         */
        private Integer type;

        /**
         * 类型名称
         */
        private String typeName;
    }
}

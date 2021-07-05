package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 视力检查结果
 *
 * @author Simple4H
 */
@Getter
@Setter
public class VisionItems {

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

    public VisionItems(String title) {
        this.title = title;
    }

    public VisionItems() {
    }

    @Getter
    @Setter
    public static class Item {
        /**
         * 视力
         */
        private BigDecimal vision;

        /**
         * 小数位视力
         */
        private String decimalVision;

        /**
         * 类型
         */
        private Integer type;
    }

}

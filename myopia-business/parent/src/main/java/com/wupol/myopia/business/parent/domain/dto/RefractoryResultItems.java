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
         * <p>
         * 裸眼视力正常： 0
         *
         * 低下： 1
         *
         * 矫正视力
         *
         * 正常： 2
         *
         * 未矫： 3
         *
         * 欠矫：4
         *
         * 验光仪
         *
         * 正常：5
         *
         * 轻度：6
         *
         * 中度：7
         *
         * 重度：8
         * </p>
         */
        private Integer type;

        /**
         * 类型名称
         */
        private String typeName;
    }
}

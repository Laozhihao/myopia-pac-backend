package com.wupol.myopia.business.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 裸眼视力详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class NakedVisionDetails {

    /**
     * 详情
     */
    private List<Item> item;


    @Getter
    @Setter
    public static class Item {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;

        /**
         * 裸眼视力
         */
        private BigDecimal vision;

        /**
         * 创建时间
         */
        private String createTime;
    }


}

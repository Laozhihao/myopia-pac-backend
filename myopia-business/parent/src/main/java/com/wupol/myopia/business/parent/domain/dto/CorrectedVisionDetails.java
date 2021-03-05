package com.wupol.myopia.business.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 矫正视力详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CorrectedVisionDetails {

    private List<Item> item;

    @Getter
    @Setter
    public static class Item {

        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;

        /**
         * 矫正视力
         */
        private BigDecimal correctedVision;

        /**
         * 创建时间
         */
        private Date createTime;
    }
}

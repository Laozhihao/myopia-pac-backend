package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
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
        private BigDecimal vision;

        /**
         * 创建时间
         */
        private String createTime;
    }
}

package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 球镜详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SphDetails {

    private List<Item> item;


    @Getter
    @Setter
    public static class Item {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;

        /**
         * 球镜
         */
        private BigDecimal vision;

        /**
         * 创建时间
         */
        private String createTime;
    }


}

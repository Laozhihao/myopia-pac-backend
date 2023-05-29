package com.wupol.myopia.business.core.stat.domain.dos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 排行榜数据
 *
 * @author Simple4H
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingDataDO implements Serializable {

    /**
     * 数据
     */
    private List<Item> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item implements Serializable {

        /**
         * 名称
         */
        private String name;

        /**
         * 近视比例
         */
        private String myopiaRadio;

        /**
         * 视力低下比例
         */
        private String lowVisionRadio;
    }
}

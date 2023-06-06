package com.wupol.myopia.business.core.stat.domain.dos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 雷达图数据
 *
 * @author Simple4H
 */
@Data
@NoArgsConstructor
public class RadarChartDataDO implements Serializable {

    /**
     * 视力底下人数
     */
    private Long lowVisionCount;

    /**
     * 筛查性近视
     */
    private Long screeningMyopiaCount;

    /**
     * 轻度近视
     */
    private Long lightMyopiaCount;

    /**
     * 高度近视
     */
    private Long highMyopiaCount;

    /**
     * 近视前期
     */
    private Long earlyMyopiaCount;

    /**
     * 散光
     */
    private Long astigmatismCount;

    /**
     * 数据
     */
    private List<Item> data;

    @Data
    public static class Item implements Serializable {

        /**
         * 描述
         */
        private String name;

        /**
         * 值
         */
        private List<Long> value;
    }

}

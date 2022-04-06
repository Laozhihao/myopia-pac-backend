package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;

/**
 * @Description 脊柱弯曲
 * @Date 2021/4/06 16:50
 * @Author by xz
 */
@Data
public class SpineDataDO {
    /**
     * 胸部
     */
    private SpineItem chest;

    /**
     * 腰部
     */
    private SpineItem waist;

    /**
     * 胸腰
     */
    private SpineItem chestWaist;

    /**
     * 前后弯曲
     */
    private SpineItem entirety;

    @Data
    public static class SpineItem {
        /**
         * 1：无侧弯。2：左低右高。3：左高右低
         */
        private Integer type;

        /**
         * 程度 1 ，2， 3
         */
        private Integer value;
    }
}

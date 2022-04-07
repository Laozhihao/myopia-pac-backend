package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.*;

import java.util.Arrays;
import java.util.List;

/**
 * @Description 龋齿
 * @Date 2021/4/06 16:50
 * @Author by xz
 */
@Data
public class SaprodontiaDataDO {
    /**
     * 上牙床
     */
    private List<SaprodontiaItem> above;

    /**
     * 下牙床
     */
    private List<SaprodontiaItem> underneath;

    @Data
    public static class SaprodontiaItem {
        /**
         * 牙齿编码
         */
        private Integer index;

        /**
         * 乳牙
         */
        private String deciduous;

        /**
         * 恒牙
         */
        private String permanent;
    }
}

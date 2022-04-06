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

    /**
     * 牙齿的缺陷类型可选项
     */
    @Getter
    @AllArgsConstructor
    public enum Type {
        D("d", "龋"),
        M("m", "失"),
        F("f", "补");

        private final String name;

        private final String flag;

        public static Type getByFlag(String flag) {
            return flag == null ? null : Arrays.stream(values()).filter((item) -> flag.equals(item.flag)).findFirst().orElse(null);
        }
    }
}

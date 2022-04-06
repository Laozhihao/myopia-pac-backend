package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * 龋齿
 */
@Data
public class SaprodontiaDataDO {
    // 上牙床
    private List<SaprodontiaItem> above;

    // 下牙床
    private List<SaprodontiaItem> underneath;

    @Data
    public static class SaprodontiaItem {
        // 牙齿编码
        private Integer index;

        // 乳牙
        private String deciduous;

        // 恒牙
        private String permanent;
    }

    // 牙齿的缺陷类型可选项
    public enum Type {
        D("d", "龋"),
        M("m", "失"),
        F("f", "补");

        Type(String flag, String name) {
            this.flag = flag;
            this.name = name;
        }

        private String name;

        private String flag;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFlag() {
            return flag;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }

        public static Type getByFlag(String flag) {
            return flag == null ? null : Arrays.stream(values()).filter((item) -> flag.equals(item.flag)).findFirst().orElse(null);
        }
    }
}

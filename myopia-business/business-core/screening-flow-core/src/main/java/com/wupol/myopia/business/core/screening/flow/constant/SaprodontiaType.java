package com.wupol.myopia.business.core.screening.flow.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 牙齿的缺陷类型可选项
 *
 * @Author wulizhou
 * @Date 2021/5/26 16:45
 */
@Getter
@AllArgsConstructor
public enum SaprodontiaType {
    D("d", "龋"),
    M("m", "失"),
    F("f", "补");

    private final String name;

    private final String flag;

    public static SaprodontiaType getByFlag(String flag) {
        return flag == null ? null : Arrays.stream(values()).filter((item) -> flag.equals(item.flag)).findFirst().orElse(null);
    }
}

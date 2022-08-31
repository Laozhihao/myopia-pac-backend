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
    DECIDUOUS_D("d", "乳牙龋"),
    DECIDUOUS_M("m", "乳牙失"),
    DECIDUOUS_F("f", "乳牙补"),
    PERMANENT_D("D", "恒牙龋"),
    PERMANENT_M("M", "恒牙失"),
    PERMANENT_F("F", "恒牙补");

    private final String name;

    private final String flag;

    public static SaprodontiaType getByFlag(String flag) {
        return flag == null ? null : Arrays.stream(values()).filter(item -> flag.equals(item.flag)).findFirst().orElse(null);
    }
}

package com.wupol.myopia.business.core.hospital;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 医院等级枚举类
 *
 * @author Simple4H
 */
@Getter
public enum HospitalLevelEnum {

    LEVEL_0(0, "一甲"),
    LEVEL_1(1, "一乙"),
    LEVEL_2(2, "一丙"),
    LEVEL_3(3, "二甲"),
    LEVEL_4(4, "二乙"),
    LEVEL_5(5, "二丙"),
    LEVEL_6(6, "三特"),
    LEVEL_7(7, "三甲"),
    LEVEL_8(8, "三乙"),
    LEVEL_9(9, "三丙"),
    LEVEL_10(10, "其他");

    private final Integer level;

    private final String levelName;

    HospitalLevelEnum(Integer level, String levelName) {
        this.level = level;
        this.levelName = levelName;
    }

    /**
     * 根据类型获取描述
     */
    public static String getLevel(Integer level) {
        HospitalLevelEnum h = Arrays.stream(HospitalLevelEnum.values())
                .filter(item -> item.level.equals(level))
                .findFirst().orElse(null);
        return Objects.nonNull(h) ? h.levelName : null;
    }
}

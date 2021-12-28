package com.wupol.myopia.business.core.hospital.constant;

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

    LEVEL_0(0, "一级甲等"),
    LEVEL_1(1, "一级乙等"),
    LEVEL_2(2, "一级丙等"),
    LEVEL_3(3, "二级甲等"),
    LEVEL_4(4, "二级乙等"),
    LEVEL_5(5, "二级丙等"),
    LEVEL_6(6, "三级特等"),
    LEVEL_7(7, "三级甲等"),
    LEVEL_8(8, "三级乙等"),
    LEVEL_9(9, "三级丙等"),
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

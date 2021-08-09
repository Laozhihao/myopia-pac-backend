package com.wupol.myopia.business.api.device.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname HyperopiaLevelEnum
 * @Description 远视程度
 * @Date 2021/8/9 6:16 下午
 * @Author Jacob
 * @Version
 */
@Getter
@AllArgsConstructor
public enum HyperopiaLevelEnum {
    NOT_LEVEL(-1, ""),
    NORMAL_LEVEL(0, "远视"),
    LOW_LEVEL(1, "轻度远视"),
    MEDIUM_LEVEL(2, "中度远视"),
    HIGH_LEVEL(3, "高度远视");
    public static final Map<Integer, String> levelDisplayMap = new HashMap(5);

    static {
        for (HyperopiaLevelEnum hyperopiaLevelEnum : EnumSet.allOf(HyperopiaLevelEnum.class)) {
            levelDisplayMap.put(hyperopiaLevelEnum.getLevel(), hyperopiaLevelEnum.getDisplay());
        }
    }

    /**
     * 等级
     */
    private int level;
    /**
     * 展示
     */
    private String display;

    /**
     * 根据等级获取展示
     *
     * @param level
     * @return
     */
    public static String getDisplayByLevel(int level) {
        return levelDisplayMap.getOrDefault(level, "");
    }
}

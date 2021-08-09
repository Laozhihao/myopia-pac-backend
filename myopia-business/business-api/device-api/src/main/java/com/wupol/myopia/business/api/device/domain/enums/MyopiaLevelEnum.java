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
public enum MyopiaLevelEnum {
    NOT_LEVEL(-1, ""),
    NORMAL_LEVEL(0, "近视"),
    LOW_LEVEL(1, "轻度近视"),
    MEDIUM_LEVEL(2, "中度近视"),
    HIGH_LEVEL(3, "高度近视");
    public static final Map<Integer, String> levelDisplayMap = new HashMap<>(5);

    static {
        for (MyopiaLevelEnum myopiaLevelEnum : EnumSet.allOf(MyopiaLevelEnum.class)) {
            levelDisplayMap.put(myopiaLevelEnum.getLevel(), myopiaLevelEnum.getDisplay());
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

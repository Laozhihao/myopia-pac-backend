package com.wupol.myopia.business.common.utils.constant;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 视力低下等级
 *
 * @author hang.yuan 2022/5/12 10:36
 */
public enum LowVisionLevelEnum {
    /**
     *
     */
    ZERO(0, "正常"),
    LOW_VISION(1, "视力低下"),
    LOW_VISION_LEVEL_LIGHT(2, "轻度视力低下"),
    LOW_VISION_LEVEL_MIDDLE(3, "中度视力低下"),
    LOW_VISION_LEVEL_HIGH(4, "重度视力低下");;

    @Getter
    public final Integer code;
    @Getter
    public final String desc;

    LowVisionLevelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public static LowVisionLevelEnum get(int code) {
        return Arrays.stream(values())
                .filter(item -> item.code == code)
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(Integer code) {
        return Optional.ofNullable(code)
                .map(LowVisionLevelEnum::get)
                .map(LowVisionLevelEnum::getDesc)
                .orElse(StrUtil.EMPTY);
    }

    public static List<LowVisionLevelEnum> lowVisionLevelList() {
        return Lists.newArrayList(LOW_VISION_LEVEL_LIGHT, LOW_VISION_LEVEL_MIDDLE, LOW_VISION_LEVEL_HIGH);
    }

    /**
     * 视力低下code集合
     */
    public static List<Integer> lowVisionLevelCodeList() {
        return Lists.newArrayList(LOW_VISION.getCode(),LOW_VISION_LEVEL_LIGHT.getCode(), LOW_VISION_LEVEL_MIDDLE.getCode(), LOW_VISION_LEVEL_HIGH.getCode());
    }
}

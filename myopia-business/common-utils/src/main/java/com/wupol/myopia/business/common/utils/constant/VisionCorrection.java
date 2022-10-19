package com.wupol.myopia.business.common.utils.constant;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public enum VisionCorrection {
    NORMAL(0, "正常"),
    UNCORRECTED(1, "未矫"),
    UNDER_CORRECTED(2, "欠矫"),
    ENOUGH_CORRECTED(3, "足矫"),
    OVER_CORRECTED(4, "过矫");

    /** 学龄段ID */
    @Getter
    public final Integer code;

    /** 学龄段描述 */
    @Getter
    public final String desc;

    VisionCorrection(Integer code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public static VisionCorrection get(Integer code) {
        return Arrays.stream(VisionCorrection.values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }


    public static String getDescByCode(Integer code) {
        if (Objects.isNull(code)) {
            return StrUtil.EMPTY;
        }
        return Optional.ofNullable(get(code))
                .filter(item->!Objects.equals(item.getCode(), NORMAL.code))
                .map(VisionCorrection::getDesc)
                .orElse(StrUtil.EMPTY);
    }
}

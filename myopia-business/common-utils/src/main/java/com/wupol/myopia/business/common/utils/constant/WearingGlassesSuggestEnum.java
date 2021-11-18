package com.wupol.myopia.business.common.utils.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 配镜建议
 *
 * @Author HaoHao
 * @Date 2021/10/28
 **/
@Getter
public enum WearingGlassesSuggestEnum {
    /** 如果code为null，则默认为无 */
    NOT_WEARING(0, "无"),
    GLASSES_SITUATION_COMMON_GLASSES(1, "配框架眼镜"),
    GLASSES_SITUATION_OK_GLASSES(2, "配OK眼镜"),
    GLASSES_SITUATION_CONTACT_LENS(3, "配隐形眼镜");

    /** 代码 */
    public final Integer code;

    /** 描述 */
    public final String desc;

    WearingGlassesSuggestEnum(Integer code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public static WearingGlassesSuggestEnum getByCode(Integer code) {
        return Arrays.stream(WearingGlassesSuggestEnum.values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(NOT_WEARING);
    }

    public static String getDescByCode(Integer code) {
        if (Objects.isNull(code)) {
            return NOT_WEARING.getDesc();
        }
        WearingGlassesSuggestEnum glassesType = getByCode(code);
        if (Objects.isNull(glassesType)) {
            return NOT_WEARING.getDesc();
        }
        return glassesType.getDesc();
    }
}

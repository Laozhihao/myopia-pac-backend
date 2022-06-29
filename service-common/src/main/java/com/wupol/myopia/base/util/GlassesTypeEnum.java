package com.wupol.myopia.base.util;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
/**
 * 戴眼镜类型
 *
 * @Author HaoHao
 * @Date 2021/10/27
 **/
@Getter
public enum GlassesTypeEnum {

    NOT_WEARING(0, "不戴镜"),
    FRAME_GLASSES(1, "框架眼镜"),
    CONTACT_LENS(2, "隐形眼镜"),
    ORTHOKERATOLOGY(3, "夜戴角膜塑形镜");

    /** 戴镜类型代码 */
    public final Integer code;

    /** 戴镜类型描述 */
    public final String desc;

    GlassesTypeEnum(Integer code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public static GlassesTypeEnum get(Integer code) {
        return Arrays.stream(GlassesTypeEnum.values())
                .filter(item -> item.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByCode(Integer code) {
        if (Objects.isNull(code)) {
            return StringUtils.EMPTY;
        }
        GlassesTypeEnum glassesType = get(code);
        if (Objects.isNull(glassesType)) {
            return StringUtils.EMPTY;
        }
        return glassesType.getDesc();
    }

    public static List<GlassesTypeEnum> glassesList() {
        return Lists.newArrayList(NOT_WEARING, FRAME_GLASSES, CONTACT_LENS, ORTHOKERATOLOGY);
    }
}

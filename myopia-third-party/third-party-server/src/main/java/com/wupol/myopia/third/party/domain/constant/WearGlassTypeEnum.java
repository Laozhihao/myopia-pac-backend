package com.wupol.myopia.third.party.domain.constant;

import com.wupol.myopia.base.util.GlassesTypeEnum;
import lombok.Getter;

import java.util.Arrays;

/**
 * 新疆戴镜类型枚举类
 * 
 * @Author lzh
 * @Date 2023/4/19
 **/
@Getter
public enum WearGlassTypeEnum {

    /** 新疆和vistel的映射关系 */
    CONTACT_LENS(0, "隐形眼镜", GlassesTypeEnum.CONTACT_LENS.getCode()),
    FRAME_GLASSES(1, "框架眼镜", GlassesTypeEnum.FRAME_GLASSES.getCode()),
    OK(2, "夜戴角膜塑形镜", GlassesTypeEnum.ORTHOKERATOLOGY.getCode());

    /** 戴镜类型代码 */
    public final Integer code;

    /** 戴镜类型描述 */
    public final String desc;

    /** vistel戴镜类型 */
    public final Integer vistelCode;

    WearGlassTypeEnum(Integer code, String desc, Integer vistelCode) {
        this.code = code;
        this.desc = desc;
        this.vistelCode = vistelCode;
    }

    /**
     * 根据vistel的戴镜类型获取code
     *
     * @param vistelGlassType vistel戴镜类型
     * @return Integer
     */
    public static Integer getCodeByVistelGlassType(Integer vistelGlassType) {
        return Arrays.stream(WearGlassTypeEnum.values()).filter(x -> x.vistelCode.equals(vistelGlassType)).map(WearGlassTypeEnum::getCode).findFirst().orElse(null);
    }
}

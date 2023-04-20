package com.wupol.myopia.third.party.util;

import com.wupol.myopia.base.util.GlassesTypeEnum;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * 新疆解析数据工具类
 * 
 * @Author lzh
 * @Date 2023/4/20
 **/
@UtilityClass
public class XinJiangParseDataUtil {

    /** 不戴镜 */
    public static final int NOT_WEAR_GLASSES = 0;
    /** 戴镜 */
    public static final int WEAR_GLASSES = 1;
    /** 缺省值9 */
    public static final String DEFAULT_STR_9 = "9";
    /** 缺省值999 */
    public static final String DEFAULT_STR_999 = "999";

    /**
     * 解析转换裸眼视力
     *
     * @param nakedVision 裸眼视力
     * @return String
     */
    public static String parseNakedVision(BigDecimal nakedVision) {
        return Optional.ofNullable(nakedVision).map(x -> x.compareTo(BigDecimal.valueOf(3)) < 0 ? DEFAULT_STR_9 : x.toString()).orElse(null);
    }

    /**
     * 获取是否带镜
     *
     * @param glassesType 戴镜类型
     * @return Integer
     */
    public static Integer getIsWear(Integer glassesType) {
        if (Objects.isNull(glassesType)) {
            return null;
        }
        return GlassesTypeEnum.NOT_WEARING.getCode().equals(glassesType) ? NOT_WEAR_GLASSES : WEAR_GLASSES;
    }

    /**
     * 解析转换sph或cyl
     *
     * @param sphOrCyl sph或cyl
     * @return String
     */
    public static String parseSphOrCyl(BigDecimal sphOrCyl) {
        return Optional.ofNullable(sphOrCyl).map(x -> x.compareTo(BigDecimal.valueOf(0)) >= 0 ? "+" + x : x.toString()).orElse(DEFAULT_STR_999);
    }

    /**
     * 解析转换
     *
     * @param axial 轴位
     * @return String
     */
    public static String parseAxial(BigDecimal axial) {
        return Optional.ofNullable(axial).map(BigDecimal::toString).orElse(DEFAULT_STR_999);
    }

    /**
     * BigDecimal转String
     *
     * @param bigDecimal 数值类型
     * @return String
     */
    public static String bigDecimalToStr(BigDecimal bigDecimal) {
        return Optional.ofNullable(bigDecimal).map(BigDecimal::toString).orElse(null);
    }
}

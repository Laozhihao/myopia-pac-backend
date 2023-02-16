package com.wupol.myopia.base.util;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 等效球镜工具类
 */
public class SEUtil {


    /**
     * 计算等效球镜 （球镜度+1/2柱镜度）
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     */
    public static BigDecimal getSphericalEquivalent(BigDecimal sphere, BigDecimal cylinder) {
        if (Objects.isNull(sphere) ) {
            return null;
        }
        return cylinder.divide(new BigDecimal(2)).add(sphere);
    }

    /**
     * 等效球镜（指定保留小数位）
     *
     * @param sphere    球镜
     * @param cylinder  柱镜
     * @param scale     保留小数位
     * @return BigDecimal
     */
    public static BigDecimal getSphericalEquivalent(BigDecimal sphere, BigDecimal cylinder, int scale) {
        BigDecimal se = getSphericalEquivalent(sphere, cylinder);
        return Objects.nonNull(se) ? se.setScale(scale, RoundingMode.HALF_UP) : se;
    }

    /**
     * 等效球镜（保留2位小数位）
     *
     * @param sphere    球镜
     * @param cylinder  柱镜
     * @return BigDecimal
     */
    public static BigDecimal getSphericalEquivalentWithTwoDecimal (BigDecimal sphere, BigDecimal cylinder) {
        return getSphericalEquivalent(sphere, cylinder, 2);
    }

    /**
     * 计算等效球镜 （球镜度+1/2柱镜度）
     *
     * @param sphere   球镜
     * @param cylinder 柱镜
     */
    public static BigDecimal getSphericalEquivalent(String sphere, String cylinder) {
        if (StringUtils.isEmpty(sphere) || StringUtils.isEmpty(cylinder)) {
            return null;
        }
        return getSphericalEquivalent(new BigDecimal(sphere), new BigDecimal(cylinder));
    }
}

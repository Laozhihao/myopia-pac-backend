package com.wupol.myopia.base.util;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

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
        if (Objects.isNull(sphere) || Objects.isNull(cylinder)) {
            return null;
        }
        return cylinder.divide(new BigDecimal(2)).add(sphere);
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

    /**
     * 等效球镜（保留2位小数位）
     *
     * @param sphere    球镜
     * @param cylinder  柱镜
     * @return BigDecimal
     */
    public static Double getSphericalEquivalent(Double sphere, Double cylinder) {
        if (Objects.isNull(sphere) || Objects.isNull(cylinder)) {
            return null;
        }
        return Optional.ofNullable(getSphericalEquivalent(new BigDecimal(sphere), new BigDecimal(cylinder))).map(BigDecimal::doubleValue).orElse(null);
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
        return Optional.ofNullable(getSphericalEquivalent(sphere, cylinder)).map(x -> x.setScale(scale, RoundingMode.HALF_UP)).orElse(null);
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
     * 等效球镜（保留2位小数位）
     *
     * @param sphere    球镜
     * @param cylinder  柱镜
     * @return BigDecimal
     */
    public static BigDecimal getSphericalEquivalentWithTwoDecimal (String sphere, String cylinder) {
        if (StringUtils.isEmpty(sphere) || StringUtils.isEmpty(cylinder)) {
            return null;
        }
        return getSphericalEquivalent(new BigDecimal(sphere), new BigDecimal(cylinder), 2);
    }

    /**
     * 等效球镜（保留2位小数位）
     *
     * @param sphere    球镜
     * @param cylinder  柱镜
     * @param defaultValue  结果为null时的默认值
     * @return BigDecimal
     */
    public static String getSphericalEquivalentWithTwoDecimal (String sphere, String cylinder, String defaultValue) {
        return Optional.ofNullable(getSphericalEquivalentWithTwoDecimal(sphere, cylinder)).map(BigDecimal::toString).orElse(defaultValue);
    }

}

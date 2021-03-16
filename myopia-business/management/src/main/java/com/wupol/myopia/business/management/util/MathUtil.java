package com.wupol.myopia.business.management.util;

import java.math.BigDecimal;

/**
 * 计算工具
 */
public class MathUtil {

    /**
     * 除法，乘100后，四舍五入保留两位小数位。如45.34%，保存45.34
     * @param numerator 分子
     * @param denominator 分母
     * @return
     */
    public static BigDecimal divide(Integer numerator, Integer denominator) {
        if (numerator == 0 || denominator == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal hundred = new BigDecimal(100);
        BigDecimal n = new BigDecimal(numerator);
        BigDecimal d = new BigDecimal(denominator);
        return n.multiply(hundred).divide(d, 2, BigDecimal.ROUND_HALF_UP);
    }
}

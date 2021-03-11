package com.wupol.myopia.business.management.util;

import java.math.BigDecimal;

/**
 * 计算工具
 */
public class MathUtil {

    /**
     * 除法，四舍五入保留两位小数位
     * @param numerator 分子
     * @param denominator 分母
     * @return
     */
    public static BigDecimal divide(Integer numerator, Integer denominator) {
        if (numerator == 0 || denominator == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal n = new BigDecimal(numerator);
        BigDecimal d = new BigDecimal(denominator);
        return n.divide(d, 2, BigDecimal.ROUND_HALF_UP);
    }

    public static void main(String[] args) {
        System.out.println(divide(3, 8));
    }

}

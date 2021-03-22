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

    /**
     * 四舍五入
     *
     * @param num
     * @return
     */
    public static double getFormatNumWith2Scale(Double num) {
        if (num == null) {
            num = 0.0D;
        }
        return  getFormatNum(2,num);
    }

    /**
     * 保留一位小数
     * @param num
     * @return
     */
    public static double getFormatNumWith1Scale(Double num) {
        if (num == null) {
            num = 0.0D;
        }
        return  getFormatNum(1,num);
    }

    /**
     * 格式化数字（四舍五入）
     * @param scale 保留几位小数
     * @param num  原数字
     * @return
     */
    private static double getFormatNum(int scale,double num) {
        BigDecimal bigDecimal = new BigDecimal(num);
        bigDecimal = bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP);
        return bigDecimal.doubleValue();
    }
}

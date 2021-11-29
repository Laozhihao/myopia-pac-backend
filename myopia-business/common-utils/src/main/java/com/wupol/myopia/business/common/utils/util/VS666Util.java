package com.wupol.myopia.business.common.utils.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2021/11/29
 **/
@UtilityClass
public class VS666Util {

    /**
     * VS666数据格式化
     *
     * @param value 值
     * @return VS666数据格式化
     */
    public static Double getDisplayValue(Double value) {
        if (Objects.isNull(value)) {
            return null;
        }
        TwoTuple<Double, Double> splitDouble = splitDouble(value);
        double result = 0d;
        Double absValue = Math.abs(splitDouble.getSecond());
        if (absValue.compareTo(0.125) < 0) {
            result = 0.00;
        }
        if (isBetweenLeft(absValue, 0.125, 0.375)) {
            result = 0.25;
        }
        if (isBetweenLeft(absValue, 0.375, 0.625)) {
            result = 0.50;
        }
        if (isBetweenLeft(absValue, 0.625, 0.875)) {
            result = 0.75;
        }
        if (absValue.compareTo(0.875) >= 0) {
            result = 1.00;
        }
        if (value.compareTo(0d) < 0) {
            result = result * (-1d);
        }
        return splitDouble.getFirst() + result;
    }

    /**
     * 拆分Double成两部分
     *
     * @param value 值
     * @return left-整数 right-小数
     */
    private static TwoTuple<Double, Double> splitDouble(Double value) {
        //整数部分
        int intNum = (int) Double.parseDouble(value.toString());
        BigDecimal valueDecimal = new BigDecimal(value.toString());

        BigDecimal intBigDecimal = new BigDecimal(intNum);
        //小数部分
        double decimalNum = valueDecimal.subtract(intBigDecimal).doubleValue();
        return new TwoTuple<>((double) intNum, decimalNum);
    }

    /**
     * 判断是否在某个区间，左闭右开区间
     *
     * @param val   值
     * @param start 开始值
     * @param end   结束值
     * @return 是否在区间内
     */
    public static boolean isBetweenLeft(Double val, Double start, Double end) {
        return val.compareTo(start) >= 0 && val.compareTo(end) < 0;
    }
}

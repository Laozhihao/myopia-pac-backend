package com.wupol.myopia.base.util;

import java.math.BigDecimal;

/**
 * BigDecimal工具类
 *
 * @author Simple4H
 */
public class BigDecimalUtil {

    /**
     * 小于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static Boolean lessThan(BigDecimal val1, String val2) {
        return val1.compareTo(new BigDecimal(val2)) < 0;
    }

    /**
     * 小于且等于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static Boolean lessThanAndEqual(BigDecimal val1, String val2) {
        return val1.compareTo(new BigDecimal(val2)) <= 0;
    }

    /**
     * 小于且等于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static Boolean lessThanAndEqual(BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) <= 0;
    }

    /**
     * 取两个值中小的
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static BigDecimal getLess(BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) <= 0 ? val1 : val2;
    }

    /**
     * 取两个值的绝对值中小的
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static BigDecimal getAbsLess(BigDecimal val1, BigDecimal val2) {
        return val1.abs().compareTo(val2.abs()) <= 0 ? val1 : val2;
    }

    /**
     * 大于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static Boolean moreThan(BigDecimal val1, String val2) {
        return val1.compareTo(new BigDecimal(val2)) > 0;
    }

    /**
     * 大于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static Boolean moreThan(BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) > 0;
    }

    /**
     * 大于且等于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static Boolean moreThanAndEqual(BigDecimal val1, String val2) {
        return val1.compareTo(new BigDecimal(val2)) >= 0;
    }

    /**
     * 大于且等于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static Boolean moreThanAndEqual(String val1, String val2) {
        return new BigDecimal(val1).compareTo(new BigDecimal(val2)) >= 0;
    }

    /**
     * 大于且等于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static Boolean moreThanAndEqual(BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) >= 0;
    }

    /**
     * 判断是否在某个区间，左闭右开区间
     *
     * @param val   值
     * @param start 开始值
     * @param end   结束值
     * @return 是否在区间内
     */
    public static Boolean isBetweenLeft(BigDecimal val, String start, String end) {
        return val.compareTo(new BigDecimal(start)) >= 0 && val.compareTo(new BigDecimal(end)) < 0;
    }

    /**
     * 判断是否在某个区间，左开右闭区间
     *
     * @param val   值
     * @param start 开始值
     * @param end   结束值
     * @return 是否在区间内
     */
    public static Boolean isBetweenRight(BigDecimal val, String start, String end) {
        return val.compareTo(new BigDecimal(start)) > 0 && val.compareTo(new BigDecimal(end)) <= 0;
    }

    /**
     * 判断是否在某个区间，左闭右闭区间
     *
     * @param val   值
     * @param start 开始值
     * @param end   结束值
     * @return 是否在区间内
     */
    public static boolean isBetweenAll(BigDecimal val, BigDecimal start, BigDecimal end) {
        return val.compareTo(start) >= 0 && val.compareTo(end) <= 0;
    }

    /**
     * 判断是否在某个区间，左闭右闭区间
     *
     * @param val   值
     * @param start 开始值
     * @param end   结束值
     * @return 是否在区间内
     */
    public static boolean isBetweenAll(BigDecimal val, String start, String end) {
        return val.compareTo(new BigDecimal(start)) >= 0 && val.compareTo(new BigDecimal(end)) <= 0;
    }

    /**
     * 判断是否在某个区间，左开右开区间
     *
     * @param val   值
     * @param start 开始值
     * @param end   结束值
     * @return 是否在区间内
     */
    public static boolean isBetweenNo(BigDecimal val, String start, String end) {
        return val.compareTo(new BigDecimal(start)) > 0 && val.compareTo(new BigDecimal(end)) < 0;
    }

    /**
     * 判断两个数是否同侧（都大于0，或都小于0）
     *
     * @param val1   值1
     * @param val2   值2
     * @param target 目标值
     * @return 是否满足
     */
    public static boolean isSameSide(BigDecimal val1, BigDecimal val2, String target) {
        return (moreThanAndEqual(val1, target) && moreThanAndEqual(val2, target)) || (lessThan(val1, target) && (lessThan(val2, target)));
    }

    /**
     * 判断两个数是都小于目标值
     *
     * @param val1   值1
     * @param val2   值2
     * @param target 目标值
     * @return 是否满足
     */
    public static boolean isAllLessThan(BigDecimal val1, BigDecimal val2, String target) {
        return lessThan(val1, target) && lessThan(val2, target);
    }

    /**
     * 判断两个数是都小于等于目标值
     *
     * @param val1   值1
     * @param val2   值2
     * @param target 目标值
     * @return 是否满足
     */
    public static boolean isAllLessThanAndEqual(BigDecimal val1, BigDecimal val2, String target) {
        return lessThanAndEqual(val1, target) && lessThanAndEqual(val2, target);
    }

    /**
     * 两数相减
     *
     * @param val1 值1
     * @param val2 值2
     * @return 结果
     */
    public static BigDecimal subtract(BigDecimal val1, BigDecimal val2) {
        return val1.subtract(val2);
    }


}

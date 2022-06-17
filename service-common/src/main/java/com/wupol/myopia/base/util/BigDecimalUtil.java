package com.wupol.myopia.base.util;

import com.google.common.collect.Maps;
import org.springframework.util.Assert;

import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Objects;

/**
 * BigDecimal工具类
 *
 * @author Simple4H
 */
public class BigDecimalUtil {

    private static Map<Integer,String> DECIMAL_FORMAT = Maps.newHashMap();

    static {
        DECIMAL_FORMAT.put(1,"0.0");
        DECIMAL_FORMAT.put(2,"0.00");
        DECIMAL_FORMAT.put(3,"0.000");
        DECIMAL_FORMAT.put(4,"0.0000");
    }

    /**
     * 小于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static boolean lessThan(BigDecimal val1, String val2) {
        return val1.compareTo(new BigDecimal(val2)) < 0;
    }

    /**
     * 等于
     *
     * @param val1 值1
     * @param val2 值2
     */
    public static boolean decimalEqual(BigDecimal val1, String val2) {
        return val1.compareTo(new BigDecimal(val2)) == 0;
    }

    public static boolean decimalEqual(BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) == 0;
    }

    /**
     * 小于且等于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static boolean lessThanAndEqual(BigDecimal val1, String val2) {
        return val1.compareTo(new BigDecimal(val2)) <= 0;
    }

    /**
     * 小于且等于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static boolean lessThanAndEqual(BigDecimal val1, BigDecimal val2) {
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
    public static boolean moreThan(BigDecimal val1, String val2) {
        return val1.compareTo(new BigDecimal(val2)) > 0;
    }

    /**
     * 大于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static boolean moreThan(BigDecimal val1, BigDecimal val2) {
        return val1.compareTo(val2) > 0;
    }

    /**
     * 大于且等于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static boolean moreThanAndEqual(BigDecimal val1, String val2) {
        return val1.compareTo(new BigDecimal(val2)) >= 0;
    }

    /**
     * 大于且等于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static boolean moreThanAndEqual(String val1, String val2) {
        return new BigDecimal(val1).compareTo(new BigDecimal(val2)) >= 0;
    }

    /**
     * 大于且等于
     *
     * @param val1 值1
     * @param val2 值2
     * @return 是否满足
     */
    public static boolean moreThanAndEqual(BigDecimal val1, BigDecimal val2) {
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
    public static boolean isBetweenLeft(BigDecimal val, String start, String end) {
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
    public static boolean isBetweenRight(BigDecimal val, String start, String end) {
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

    /**
     * 视力是否误差(判断有一个值为null，不作误差值判断)
     * @param firstScreening 视力误差
     * @param reScreening 复测值
     * @param standard 标准值
     * @return true：误差 false：没误差
     */
    public static boolean isDeviation(BigDecimal firstScreening, BigDecimal reScreening, BigDecimal standard){
        BigDecimal result = subtractAbsBigDecimal(firstScreening, reScreening);
        return Objects.isNull(result) ? Boolean.FALSE : result.abs().compareTo(standard) > 0;
    }

    /**
     * 绝对差值
     * @param firstScreening 初测值
     * @param reScreening 复测值
     * @return 绝对差值
     */
    public static BigDecimal subtractAbsBigDecimal(BigDecimal firstScreening, BigDecimal reScreening) {
        return ObjectUtils.allNotNull(firstScreening, reScreening) ? firstScreening.subtract(reScreening).abs() : null;
    }

    /**
     *
     * @param v1 分子
     * @param v2 分母
     * @param scale 精确小数
     */
    public static BigDecimal divide(String v1, String v2, int scale) {
        Assert.notNull(v1,"can not null");
        Assert.notNull(v2,"can not null");
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return divide(b1,b2,scale);
    }

    public static BigDecimal divide(BigDecimal b1, BigDecimal b2, int scale) {
        Assert.notNull(b1,"can not null");
        Assert.notNull(b2,"can not null");
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        if (BigDecimal.ZERO.compareTo(b2) == 0){
            return new BigDecimal("0.0");
        }
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 响应前端时使用
     * @param source 原数据
     * @param scale 保留几位小数
     */
    public static BigDecimal getBigDecimalByFormat(BigDecimal source,int scale){
        if (Objects.isNull(source)){
            return null;
        }
        DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT.get(scale));
        return new BigDecimal(decimalFormat.format(source));
    }

}

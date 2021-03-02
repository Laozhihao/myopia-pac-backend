package com.wupol.myopia.base.util;

import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期工具
 *
 * @Author HaoHao
 * @Date 2020/12/20
 */
@UtilityClass
public class DateUtil {

    /**
     * 获取当前时间的年与上一个月,若当前是1月,则取得上年12月
     *
     * @return java.util.Map<java.lang.String,java.lang.Integer>
     **/
    public static Map<String, Integer> getCurrentTimeLastMonth() {
        Map<String, Integer> resultMap = new HashMap<>(3);
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        if (month == 0) {
            year = year - 1;
            month = 12;
        }
        resultMap.put("year", year);
        resultMap.put("month", month);
        return resultMap;
    }

    /**
     * 比较两个日期是否相等
     *
     * @param c1
     * @param c2
     * @return java.lang.Boolean
     **/
    public static Boolean isDateEqual(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 判断两个日期是否相等
     *
     * @param d1
     * @param d2
     * @return java.lang.Boolean
     */
    public static Boolean isDateEqual(Date d1, Date d2) {
        if (d1 == d2) {
            return true;
        }
        if (d1 != null && d2 != null) {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(d1);
            Calendar c2 = Calendar.getInstance();
            c2.setTime(d2);
            return isDateEqual(c1, c2);
        }
        return false;
    }

    /**
     * 获取指定日期的年月日
     *
     * @param date
     * @return java.util.Map<java.lang.String,java.lang.Integer>
     **/
    public static Map<String, Integer> processYearMonthDay(Date date) {
        Map<String, Integer> result = new HashMap<>(4);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        result.put("year", cal.get(Calendar.YEAR));
        result.put("month", cal.get(Calendar.MONTH) + 1);
        result.put("day", cal.get(Calendar.DAY_OF_MONTH));
        return result;
    }

    /**
     * 获取当前日期时间
     *
     * @return java.lang.String
     **/
    public static String getNowDateTimeStr() {
        return DateFormatUtil.format(new Date(), DateFormatUtil.FORMAT_DETAIL_TIME);
    }

    /**
     * 把LocalDateTime转为Date类型
     *
     * @param localDateTime
     * @return java.util.Date
     **/
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 把LocalDate转为Date类型
     *
     * @param localDate
     * @return java.util.Date
     **/
    public static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取当前时间加上所传入毫秒值后的时间
     *
     * @param millis 需要增加的时间的毫秒值
     * @return ava.util.Date
     */
    public static Date getDateAhead(Long millis) {
        Date now = new Date();
        now.setTime(System.currentTimeMillis() + millis);
        return now;
    }

    /**
     * 获取今天零晨的时间戳
     *
     * @param timezone  时区，格式如：GMT+2、GMT+0200、+2、+0200
     * @return long
     **/
    public static long getTodayStartTime(String timezone) {
        LocalDateTime todayStartTime = LocalDateTime.of(LocalDate.now(ZoneId.of(timezone)), LocalTime.MIN);
        return todayStartTime.atZone(ZoneId.of(timezone)).toEpochSecond()  * 1000;
    }

    /**
     * 获取昨天零晨的时间戳
     *
     * @param timezone  时区，格式如：GMT+2、GMT+0200、+2、+0200
     * @return long
     **/
    public static long getYesterdayStartTime(String timezone) {
        LocalDateTime todayStartTime = LocalDateTime.of(LocalDate.now(ZoneId.of(timezone)), LocalTime.MIN);
        return todayStartTime.minusDays(1).atZone(ZoneId.of(timezone)).toEpochSecond()  * 1000;
    }

    /**
     * 获取当前月份1号零晨的时间戳
     *
     * @param timezone  时区，格式如：GMT+2、GMT+0200、+2、+0200
     * @return long
     **/
    public static long getFirstDayOfCurrMonth(String timezone) {
        LocalDateTime todayStartTime = LocalDateTime.of(LocalDate.now(ZoneId.of(timezone)), LocalTime.MIN);
        LocalDateTime firstDayOfCurrMonth = todayStartTime.with(TemporalAdjusters.firstDayOfMonth());
        return firstDayOfCurrMonth.atZone(ZoneId.of(timezone)).toEpochSecond()  * 1000;
    }

    /**
     * 获取上个月1号零晨的时间戳
     *
     * @param timezone  时区，格式如：GMT+2、GMT+0200、+2、+0200
     * @return long
     **/
    public static long getFirstDayOfLastMonth(String timezone) {
        LocalDateTime todayStartTime = LocalDateTime.of(LocalDate.now(ZoneId.of(timezone)), LocalTime.MIN);
        LocalDateTime firstDayOfLastMonth = todayStartTime.with(TemporalAdjusters.firstDayOfMonth());
        return firstDayOfLastMonth.minusMonths(1).atZone(ZoneId.of(timezone)).toEpochSecond()  * 1000;
    }

    /**
     * 将timestamp转为LocalDateTime
     *
     * @param timestamp 时间戳
     * @param timezone  时区，格式如：GMT+2、GMT+0200、+2、+0200
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime timestampToDatetime(long timestamp, String timezone){
        Instant instant = Instant.ofEpochMilli(timestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.of(timezone));
    }

    /**
     * 该日期时间是否今天0点之前
     *
     * @param date
     * @return
     */
    public static boolean isDateBeforeToday(Date date) {
        return date.getTime() < DateUtil.getTodayStartTime("GMT+8");
    }
}

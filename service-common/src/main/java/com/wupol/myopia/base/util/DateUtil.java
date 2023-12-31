package com.wupol.myopia.base.util;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import com.wupol.myopia.base.exception.BusinessException;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * 日期工具
 *
 * @Author HaoHao
 * @Date 2020/12/20
 */
@UtilityClass
public class DateUtil extends cn.hutool.core.date.DateUtil {

    public final String UTC_8 = "UTC+8";
    public final ZoneId ZONE_UTC_8 = ZoneId.of(UTC_8);

    /**
     * 获取当前时间的年与上一个月,若当前是1月,则取得上年12月
     *
     * @return java.util.Map<java.lang.String, java.lang.Integer>
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
     * @return java.util.Map<java.lang.String, java.lang.Integer>
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
     * @param timezone 时区，格式如：GMT+2、GMT+0200、+2、+0200
     * @return long
     **/
    public static long getTodayStartTime(String timezone) {
        LocalDateTime todayStartTime = LocalDateTime.of(LocalDate.now(ZoneId.of(timezone)), LocalTime.MIN);
        return todayStartTime.atZone(ZoneId.of(timezone)).toEpochSecond() * 1000;
    }

    /**
     * 获取昨天零晨的时间戳
     *
     * @param timezone 时区，格式如：GMT+2、GMT+0200、+2、+0200
     * @return long
     **/
    public static long getYesterdayStartTime(String timezone) {
        LocalDateTime todayStartTime = LocalDateTime.of(LocalDate.now(ZoneId.of(timezone)), LocalTime.MIN);
        return todayStartTime.minusDays(1).atZone(ZoneId.of(timezone)).toEpochSecond() * 1000;
    }

    /**
     * 获取昨天零晨的时间
     *
     * @return long
     **/
    public static Date getYesterdayStartTime() {
        return getStartTime(DateUtils.addDays(new Date(), -1));
    }

    public static Date getStartTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取最迟时刻的时间
     *
     * @return long
     **/
    public static Date getEndTime(Date date) {
        Calendar cal=Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * 获取指定时间最后一分钟
     *
     * @return long
     **/
    public static Date getLastMinute(Date date) {
        Calendar cal=Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取指定日期的中午时间（12点整）
     *
     * @param date
     * @return
     */
    public static Date getMidday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取昨天最迟一秒的时间
     *
     * @return long
     **/
    public static Date getYesterdayEndTime() {
        return getEndTime(DateUtils.addDays(new Date(), -1));
    }

    /**
     * 获取当前月份1号零晨的时间戳
     *
     * @param timezone 时区，格式如：GMT+2、GMT+0200、+2、+0200
     * @return long
     **/
    public static long getFirstDayOfCurrMonth(String timezone) {
        LocalDateTime todayStartTime = LocalDateTime.of(LocalDate.now(ZoneId.of(timezone)), LocalTime.MIN);
        LocalDateTime firstDayOfCurrMonth = todayStartTime.with(TemporalAdjusters.firstDayOfMonth());
        return firstDayOfCurrMonth.atZone(ZoneId.of(timezone)).toEpochSecond() * 1000;
    }

    /**
     * 获取上个月1号零晨的时间戳
     *
     * @param timezone 时区，格式如：GMT+2、GMT+0200、+2、+0200
     * @return long
     **/
    public static long getFirstDayOfLastMonth(String timezone) {
        LocalDateTime todayStartTime = LocalDateTime.of(LocalDate.now(ZoneId.of(timezone)), LocalTime.MIN);
        LocalDateTime firstDayOfLastMonth = todayStartTime.with(TemporalAdjusters.firstDayOfMonth());
        return firstDayOfLastMonth.minusMonths(1).atZone(ZoneId.of(timezone)).toEpochSecond() * 1000;
    }

    /**
     * 将timestamp转为LocalDateTime
     *
     * @param timestamp 时间戳
     * @param timezone  时区，格式如：GMT+2、GMT+0200、+2、+0200
     * @return java.time.LocalDateTime
     **/
    public static LocalDateTime timestampToDatetime(long timestamp, String timezone) {
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
        return date.getTime() < DateUtil.getTodayStartTime(TimeZone.getDefault().getID());
    }

    /**
     * 格式判断
     *
     * @param sDate
     * @return
     */
    public static String isValidDate(String sDate) {
        if (!RegExpUtil.isDate(sDate)) {
            return null;
        }
        String s = RegExpUtil.convertDate(sDate);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            format.setLenient(false); // 严格检查
            Date parse = format.parse(s);
            Date date = new Date();
            if (parse.getTime() - date.getTime() > 0) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
        return s;
    }

    /**
     * 根据时间获取年份
     *
     * @param date
     * @return
     */
    public Integer getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 根据日期获取其所属学年，同一学年：9月1日到次年8月31日
     *
     * @param date 日期
     * @return int 所属学年，如：2021、2017
     **/
    public int getSchoolYear(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int month = localDate.getMonthValue();
        return month >= 9 ? localDate.getYear() : localDate.getYear() - 1;
    }

    /**
     * Date to LocalDate
     *
     * @param date   日期
     * @param zoneId 时区ID
     * @return
     */
    public static LocalDate convertToLocalDate(Date date, ZoneId zoneId) {
        Instant instant = date.toInstant();
        ZonedDateTime zdt = instant.atZone(zoneId);
        return zdt.toLocalDate();
    }

    /**
     * 获取多少天前后的 yyyyD格式的日期字符串
     *
     * @param date
     * @return
     */
    public static String getDayOfYear(Date date, int offsetDay) {
        Date date30DaysAgo = DateUtils.addDays(date, offsetDay);
        return DateFormatUtils.format(date30DaysAgo, "yyyyD");
    }

    /**
     * 获取今天特定的时分
     *
     * @param hourOfDay
     * @param mins
     * @return
     */
    public static Date getTodayTime(int hourOfDay, int mins) {
        return getSpecialDateTime(hourOfDay, mins, 0);
    }

    /**
     * 获取特定日期以及日期的时分
     *
     * @param hourOfDay
     * @param mins
     * @param offsetDays
     * @return
     */
    public static Date getSpecialDateTime(int hourOfDay, int mins, int offsetDays) {
        return getSpecialDateTime(hourOfDay, mins, 0, offsetDays);
    }


    /**
     * 获取特定日期以及日期的时分秒
     *
     * @param hourOfDay
     * @param mins
     * @param seconds
     * @param offsetDays
     * @return
     */
    public static Date getSpecialDateTime(int hourOfDay, int mins, int seconds, int offsetDays) {
        DateTime dateTime = cn.hutool.core.date.DateUtil.beginOfDay(new Date());
        dateTime = cn.hutool.core.date.DateUtil.offsetDay(dateTime, offsetDays);
        dateTime.setField(DateField.HOUR_OF_DAY, hourOfDay);
        dateTime.setField(DateField.SECOND, seconds);
        dateTime.setField(DateField.MINUTE, mins);
        return dateTime;
    }

    /**
     * 获取年龄描述
     * <p>
     * 0<*<3 ：显示月龄+天数，如3个月1天、3个月10天
     * 3≤*7 ：显示岁龄+月数+天数，如3岁1个月1天
     * *≥7：显示年龄，如7岁
     * </p>
     *
     * @param startDate 时间
     * @return String
     */
    public static String getAgeInfo(Date startDate, Date endDate) {
        if (Objects.isNull(startDate)) {
            return StringUtils.EMPTY;
        }
        Period period = Period.between(startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        if (years >= 0 && years < 3) {
            return (years * 12 + months) + "个月" + days + "天";
        }
        if (years >= 3 && years < 7) {
            return years + "岁" + months + "个月" + days + "天";
        }
        if (years >= 7) {
            return years + "岁";
        }
        return StringUtils.EMPTY;
    }

    /**
     * 计算end比start晚的天数<br/>
     * 如(2021-12-13, 2021-12-15) = 2
     * 如(2021-12-16, 2021-12-15) = -1
     * @param start
     * @param end
     * @return
     */
    public static long betweenDay(Date start, Date end) {
        Date startTime = getStartTime(start);
        Date endTime = getStartTime(end);
        long diff = endTime.getTime() - startTime.getTime();
        return diff / DateUnit.DAY.getMillis();
    }

    /**
     * 距离结束的天数
     * @param start 开始时间
     * @param end   结束时间
     * @return
     */
    public static Integer getRemainTime(Date start, Date end) {
        if (Objects.isNull(start) || Objects.isNull(end)) {
            return null;
        }
        Date now = new Date();
        return start.getTime() < now.getTime() ? Math.max(0, (int) betweenDay(now, end)) :
                Math.max(0, (int) betweenDay(start, end));
    }

    /**
     * 生日是否超出限制
     *
     * @param date 生日
     */
    public void checkBirthday(Date date) {
        // 1970-01-01 00:00:02 毫秒时间戳
        Date beforeDate = new Date(-28798000L);
        Date afterDate = new Date();
        if (Objects.nonNull(date) && (date.before(beforeDate) || date.after(afterDate))) {
            throw new BusinessException(DateFormatUtil.format(date, DateFormatUtil.FORMAT_ONLY_DATE2) + "生日超出限制，请确认");
        }
    }

    /**
     * 判断时间是否在时间段内
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     *
     * @return 是否在时间段内
     */
    public static boolean isBetweenDate(Date beginTime, Date endTime) {
        Date nowTime = new Date();
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        return date.after(begin) && date.before(end);
    }

    /**
     * 判断时间是否在时间段内
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     *
     * @return 是否在时间段内
     */
    public static boolean isBetweenDate(Date beginTime, Date endTime, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        return calendar.after(begin) && calendar.before(end);
    }

    /**
     * 当前系统的默认入学年份
     */
    public static Integer getSchoolYear(){
        LocalDate localDate = LocalDate.now();
        int monthValue = localDate.getMonthValue();
        return Objects.equals(monthValue,1)?localDate.getYear()-1:localDate.getYear();
    }

}

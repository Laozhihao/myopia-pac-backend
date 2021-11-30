package com.wupol.myopia.base.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期格式化工具
 *
 * @Author HaoHao
 * @Date 2020/12/20
 */
public final class DateFormatUtil {
    public static final String FORMAT_ONLY_DATE = "yyyy-MM-dd";
    public static final String FORMAT_ONLY_DATE2 = "yyyy/MM/dd";
    public static final String FORMAT_ONLY_TIME = "HH:mm:ss";
    public static final String FORMAT_TIME_WITHOUT_SECOND = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_ONLY_HOUR_MINUTE = "HH:mm";
    public static final String FORMAT_DETAIL_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.S";
    public static final String TIMESTAMP_FORMAT_UNDERLINE = "yyyy-MM-dd_HH_mm_ss_S";
    public static final String FORMAT_TIME_WITHOUT_LINE = "yyyyMMddHHmmssS";
    public static final String FORMAT_DATE_AND_TIME_WITHOUT_SEPERATOR = "yyyyMMddHHmmss";
    public static final String FORMAT_ONLY_DATE_WITHOUT_LINE = "yyyyMMdd";
    public static final String FORMAT_ONLY_DATE_WITH_CHINESE = "MM月dd日";
    public static final String FORMAT_ONLY_MONTH_DATE = "MM/dd";
    public static final String FORMAT_ONLY_YEAR_MONTH = "yyyy-MM";
    public static final String FORMAT_ONLY_DAY = "dd";

    private DateFormatUtil() {
    }

    /**
     * 解析日期字符串为日期类型
     *
     * @param dateStr
     * @param dateFormat
     * @return java.lang.String
     **/
    public static Date parseDate(String dateStr, String dateFormat) throws ParseException {
        if (dateStr == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        //设置SimpleDateFormat的宽容解析为false,传入的dateStr必须严格按照pattern解析
        sdf.setLenient(false);
        return sdf.parse(dateStr);
    }

    /**
     * 格式化指定日期
     *
     * @param date
     * @param dateFormat
     * @return java.lang.String
     **/
    public static String format(Date date, String dateFormat) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(dateFormat).format(date);
    }

    /**
     * 格式化当前日期为指定格式
     *
     * @param dateFormat
     * @return java.lang.String
     **/
    public static String formatNow(String dateFormat) {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        return format(now, dateFormat);
    }

    /**
     * 格式化指定日期
     *
     * @param temporal      如：LocalDate、LocalDateTime
     * @param dateFormat    日期格式
     * @return java.lang.String
     **/
    public static String format(TemporalAccessor temporal, String dateFormat) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(dateFormat);
        return df.format(temporal);
    }

}

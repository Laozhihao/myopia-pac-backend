package com.wupol.myopia.rec.server.util;

import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具
 *
 * @author hang.yuan 2022/8/12 16:17
 */
public class DateUtil extends cn.hutool.core.date.DateUtil {

    /**
     * 获取最近日期
     * @param hours 小时
     */
    public static Date getRecentDate(Integer hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }
}

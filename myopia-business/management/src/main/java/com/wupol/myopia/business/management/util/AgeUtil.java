package com.wupol.myopia.business.management.util;

import com.wupol.framework.core.util.DateUtil;

import java.time.LocalDate;
import java.time.Period;
import java.util.Date;

/**
 * Created by sheldon on 20-11-16.
 *
 * @author sheldon
 */
public class AgeUtil {
    public static String getPatientAgeDisplay(Date birthday, Boolean needEnglish) {
        if (birthday == null) {
            return needEnglish? "Unknown" : "未知";
        }
        return countAge(birthday, LocalDate.now(), needEnglish);
    }

    public static String getPatientAgeDisplayFromCertainDay(Date birthday, Date certainDay) {
        if (birthday == null || certainDay == null) {
            return "未知";
        }
        return countAge(birthday, DateUtil.fromDate(certainDay), false);
    }

    private static String countAge(Date birthday, LocalDate compareDate, Boolean needEnglish) {
        LocalDate birthLocalDate = DateUtil.fromDate(birthday);
        Period period = Period.between(birthLocalDate, compareDate);
        int years = period.getYears();
        if (years > 0) {
            return years + (needEnglish? " Years" : "岁");
        }
        int months = period.getMonths();
        if (months > 0) {
            return months + (needEnglish? " Months" : "个月");
        }
        return period.getDays() + (needEnglish? " Days" : "天");
    }

    public static Integer countAge(Date birthday) {
        LocalDate birthLocalDate = DateUtil.fromDate(birthday);
        Period period = Period.between(birthLocalDate, LocalDate.now());
        return period.getYears();
    }
}

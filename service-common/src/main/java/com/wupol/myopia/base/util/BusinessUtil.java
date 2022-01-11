package com.wupol.myopia.base.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import com.wupol.myopia.base.constant.MonthAgeEnum;

import java.time.Period;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Author wulizhou
 * @Date 2021/12/28 16:04
 */
public class BusinessUtil {

    /**
     * 检验合作信息是否有效
     *
     * @param cooperationType
     * @param cooperationTimeType
     * @param cooperationStartTime
     * @param cooperationEndTime
     * @return
     */
    public static boolean checkCooperation(Integer cooperationType, Integer cooperationTimeType, Date cooperationStartTime,
                                           Date cooperationEndTime) {
        if (Objects.isNull(cooperationType) || Objects.isNull(cooperationTimeType) || Objects.isNull(cooperationStartTime)
                || Objects.isNull(cooperationEndTime)) {
            return false;
        }
        if (cooperationStartTime.getTime() > cooperationEndTime.getTime()) {
            return false;
        }
        return true;
    }

    /**
     * 获取用户可做的月龄检查
     *
     * @param birthday 生日
     * @return 满足条件的mouthDay
     */
    public static List<Integer> getCanCheckMonthAgeByDate(Date birthday) {
        Date now = new Date();
        Period period = Period.between(birthday.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                now.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        // 6岁<*【6岁】
        if (years >= 6 && (months > 0 || days > 0)) {
            return Arrays.asList(MonthAgeEnum.YEAR6.getId());
        } else if (years == 5 && (months > 0 || days > 0)) {
            // 5岁＜*≤6岁：【5岁】【6岁】
            return Arrays.asList(MonthAgeEnum.YEAR5.getId(), MonthAgeEnum.YEAR6.getId());
        } else if (years == 4 && (months > 0 || days > 0)) {
            // 4岁＜*≤5岁：【4岁】【5岁】
            return Arrays.asList(MonthAgeEnum.YEAR4.getId(), MonthAgeEnum.YEAR5.getId());
        } else if (years == 3 && (months > 0 || days > 15)) {
            // 36月龄15天＜*≤4岁：【36月龄】【4岁】
            return Arrays.asList(MonthAgeEnum.MONTH36.getId(), MonthAgeEnum.YEAR4.getId());
        } else if ((years * 12 + months > 35) || (years * 12 + months == 35 && days > 15)) {
            // 35月龄15天＜*≤36月龄15天：【36月龄】
            return Arrays.asList(MonthAgeEnum.MONTH36.getId());
        } else if (years == 2 && (months > 0 || days > 15)) {
            // 24月龄15天＜*≤35月龄15天：【24月龄】、【36月龄】
            return Arrays.asList(MonthAgeEnum.MONTH24.getId(), MonthAgeEnum.MONTH36.getId());
        } else if ((years * 12 + months > 23) || (years * 12 + months == 23 && days > 15)) {
            // 23月龄15天＜*≤24月龄15天：【24月龄】
            return Arrays.asList(MonthAgeEnum.MONTH24.getId());
        } else if ((years * 12 + months > 18) || (years * 12 + months == 18 && days > 15)) {
            // 18月龄15天＜*≤23月龄15天：【18月龄】、【24月龄】
            return Arrays.asList(MonthAgeEnum.MONTH18.getId(), MonthAgeEnum.MONTH24.getId());
        } else if ((years * 12 + months > 17) || (years * 12 + months == 17 && days > 15)) {
            // 17月龄15天＜*≤18月龄15天：【18月龄】
            return Arrays.asList(MonthAgeEnum.MONTH18.getId());
        } else if ((years * 12 + months > 12) || (years * 12 + months == 12 && days > 15)) {
            // 12月龄15天＜*≤17月龄15天：【12月龄】、【18月龄】
            return Arrays.asList(MonthAgeEnum.MONTH12.getId(), MonthAgeEnum.MONTH18.getId());
        } else if ((years * 12 + months > 11) || (years * 12 + months == 11 && days > 15)) {
            // 11月龄15天＜*≤12月龄15天：【12月龄】
            return Arrays.asList(MonthAgeEnum.MONTH12.getId());
        } else if ((months > 8) || (months == 8 && days > 15)) {
            // 8月龄15天＜*≤11月龄15天：【8月龄】、【12月龄】
            return Arrays.asList(MonthAgeEnum.MONTH8.getId(), MonthAgeEnum.MONTH12.getId());
        } else if ((months > 7) || (months == 7 && days > 15)) {
            // 7月龄15天＜*≤8月龄15天：【8月龄】
            return Arrays.asList(MonthAgeEnum.MONTH8.getId());
        } else if ((months > 6) || (months == 6 && days > 15)) {
            // 6月龄15天＜*≤7月龄15天：【6月龄】、【8月龄】
            return Arrays.asList(MonthAgeEnum.MONTH6.getId(), MonthAgeEnum.MONTH8.getId());
        } else if ((months > 5) || (months == 5 && days > 15)) {
            // 5月龄15天＜*≤6月龄15天：【6月龄】
            return Arrays.asList(MonthAgeEnum.MONTH6.getId());
        } else if ((months > 3) || (months == 3 && days > 15)) {
            // 3月龄15天＜*≤5月龄15天：【3月龄】、【6月龄】
            return Arrays.asList(MonthAgeEnum.MONTH3.getId(), MonthAgeEnum.MONTH6.getId());
        } else if ((months > 2) || (months == 2 && days > 15)) {
            // 2月龄15天＜*≤3月龄15天：【3月龄】
            return Arrays.asList(MonthAgeEnum.MONTH3.getId());
        } else if (DateUtil.betweenDay(birthday, now) > 45) {
            // 45天＜*≤2月龄15天：【满月】、【3月龄】
            return Arrays.asList(MonthAgeEnum.MONTH1.getId(), MonthAgeEnum.MONTH3.getId());
        } else if (days > 15) {
            // 45天＜*≤2月龄15天：【满月】、【3月龄】
            return Arrays.asList(MonthAgeEnum.MONTH1.getId());
        } else {
            // 0＜*≤15天：【新生儿】
            return Arrays.asList(MonthAgeEnum.NB.getId());
        }
    }

    /**
     * 通过生日获取年龄段
     *
     * @param birthday 生日
     * @return {@link MonthAgeEnum} 枚举类
     */
    public static MonthAgeEnum getMonthAgeByBirthday(Date birthday) {
        if (isMatchNewBorn(birthday)) {
            return MonthAgeEnum.NB;
        }
        if (isMatchMonth1(birthday)) {
            return MonthAgeEnum.MONTH1;
        }
        List<MonthAgeEnum> monthAgeList = MonthAgeEnum.monthAgeList;
        for (MonthAgeEnum monthAgeEnum : monthAgeList) {
            if (isMatchMouthDay(birthday, monthAgeEnum.getOffset())) {
                return monthAgeEnum;
            }
        }
        return null;
    }

    /**
     * 是否满足新生儿条件
     *
     * @param birthday 生日
     * @return 是否满足
     */
    private static boolean isMatchNewBorn(Date birthday) {
        Date nowDate = new Date();
        return DateUtil.isSameDay(nowDate, birthday)
                || DateUtil.isSameDay(nowDate, DateUtil.offsetDay(birthday, 3))
                || DateUtil.isSameDay(nowDate, DateUtil.offsetDay(birthday, 7));
    }

    /**
     * 是否满足满月
     *
     * @param birthday 生日
     * @return 是否满足
     */
    private static boolean isMatchMonth1(Date birthday) {
        long betweenDay = DateUtil.between(birthday, new Date(), DateUnit.DAY);
        return betweenDay == 23 || betweenDay == 27 || betweenDay == 30 || betweenDay == 33 || betweenDay == 37;
    }

    /**
     * 月龄是否满足条件
     *
     * @param birthday 生日
     * @param offset   年龄段
     * @return 是否满足
     */
    private static boolean isMatchMouthDay(Date birthday, Integer offset) {

        DateTime checkDay = DateUtil.offsetMonth(birthday, offset);
        Date nowDate = new Date();
        return DateUtil.isSameDay(nowDate, DateUtil.offsetDay(checkDay, -7))
                || DateUtil.isSameDay(nowDate, DateUtil.offsetDay(checkDay, -3))
                || DateUtil.isSameDay(nowDate, checkDay)
                || DateUtil.isSameDay(nowDate, DateUtil.offsetDay(checkDay, 3))
                || DateUtil.isSameDay(nowDate, DateUtil.offsetDay(checkDay, 7));
    }
}

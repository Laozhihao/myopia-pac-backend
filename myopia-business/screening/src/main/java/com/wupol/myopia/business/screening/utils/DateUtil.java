package com.wupol.myopia.business.screening.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class DateUtil {

    /**
     * 计算时间差，以天数为单位。如：2018-08-08 和 2018-08-05 相差3天
     * @param startTime
     * @param endTime
     * @return
     */
    public static int getDistanceTime(Date startTime, Date endTime) {
        int days = 0;
        long time1 = startTime.getTime();
        long time2 = endTime.getTime();

        long diff;
        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        days = (int) (diff / (24 * 60 * 60 * 1000));
        return days;
    }

    /**
     * 计算时间差，以小时为单位。如：2018-08-08 和 2018-08-07 相差24h
     * @param startTime
     * @param endTime
     * @return
     */
    public static double getDistanceTime2(Date startTime, Date endTime) {
        double hour = 0;
        long time1 = startTime.getTime();
        long time2 = endTime.getTime();

        long diff;
        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        hour = (diff / (60 * 60 * 1000));
        return hour;
    }

    /**
     * 获取当前时间
     * @return
     */
    public static String getCurrent(){

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

        return df.format(new Date());
    }
    /**
     * 获取当前时间
     * @return
     */
    public static String getLoadDate(){
        //可以抽取为日期工具类
        Date date1 = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = df.format(date1);
        return date;
    }

    public static String dateTodate(Date currentTime){

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");

        String time = formatter.format(currentTime);

        return time;
    }
    public static String dateTodate2(Date currentTime){

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        String time = formatter.format(currentTime);

        return time;
    }

    public static String dateTodate3(Date currentTime){

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String time = formatter.format(currentTime);

        return time;
    }

    public static int getYearbyAge(int age){

        Calendar cal = Calendar.getInstance();

        int yearNow = cal.get(Calendar.YEAR);  //当前年份

        return yearNow-age;
    }

    /**
     * 格式判断
     * @param sDate
     * @return
     */
    public static String isValidDate(String sDate){
        if (!ReUtil.isDate(sDate)){
            return null;
        }
        String s = ReUtil.convertDate(sDate);
        SimpleDateFormat format= new SimpleDateFormat("yyyy-MM-dd");
        try{
            format.setLenient(false); // 严格检查
            Date parse = format.parse(s);
            Date date = new Date();
            if (parse.getTime()-date.getTime()>0){
                return null;
            }
        }catch(Exception e){
            return null;
        }
        return s;
    }
    public static  Date parse(String strDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static  Date parse1(String strDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getAge(Date birthDay){
        if (birthDay==null){
            return 0;
        }

        Calendar cal = Calendar.getInstance();
        if (cal.before(birthDay)) { //出生日期晚于当前时间，无法计算
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        int yearNow = cal.get(Calendar.YEAR);  //当前年份
        int monthNow = cal.get(Calendar.MONTH);  //当前月份
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH); //当前日期
        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
        int age = yearNow - yearBirth;
        if (monthNow <= monthBirth){
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) age--;//当前日期在生日之前，年龄减一
            }else{
                age--;//当前月份在生日之前，年龄减一
            }
        }
        return age;
    }

}

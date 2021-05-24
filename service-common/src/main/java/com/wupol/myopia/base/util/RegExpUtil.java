package com.wupol.myopia.base.util;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则匹配验证
 */
@UtilityClass
public class RegExpUtil {

    /**
     * 日期匹配规则
     */
    private static final String DATE1 = "^\\d{4}\\-\\d{1,2}\\-\\d{1,2}$";
    private static final String DATE2 = "^\\d{4}\\/\\d{1,2}\\/\\d{1,2}$";
    private static final String DATE3 = "^\\d{4}\\u5e74\\d{1,2}\\u6708\\d{1,2}\\u65e5$";
    private static final String DATE4 = "^\\d{4}\\u5e74\\d{1,2}\\u6708$";
    private static final String DATE5 = "^\\d{8}$";

    /**
     * 日期匹配
     */
    public static String convertDate(String date){
        int length = date.length();
        Pattern date1 = Pattern.compile(DATE1);
        Pattern date2 = Pattern.compile(DATE2);
        Pattern date3 = Pattern.compile(DATE3);
        Pattern date4 = Pattern.compile(DATE4);
        Pattern date5 = Pattern.compile(DATE5);
        Matcher matcherDate1 = date1.matcher(date);
        Matcher matcherDate2 = date2.matcher(date);
        Matcher matcherDate3 = date3.matcher(date);
        Matcher matcherDate4 = date4.matcher(date);
        if (matcherDate1.find()){
            if (length==10){
                return date;
            }else if (length==9){
                if (!date.substring(5,6).equals("0")){
                    return date.substring(0,5)+"0"+date.substring(5);
                }else {
                    return date.substring(0,8)+"0"+date.substring(8);
                }
            }else{
                return date.substring(0,5)+"0"+date.substring(5,7)+"0"+date.substring(7);
            }

        }else if (matcherDate2.find()){
            String sub;
            if (length==10){
                return date.replace("/","-");
            }else if (length==9){
                if (!date.substring(5,6).equals("0")){
                    sub = date.substring(0,5)+"0"+date.substring(5);
                }else {
                    sub = date.substring(0,8)+"0"+date.substring(8);
                }
            }else{
                sub = date.substring(0,5)+"0"+date.substring(5,7)+"0"+date.substring(7);
            }
            return sub.replace("/","-");
        }else if (matcherDate3.find()) {
            String sub;
            if (length==11){
                sub = date;
            }else if (length==10){
                if (!date.substring(5,6).equals("0")){
                    sub = date.substring(0,5)+"0"+date.substring(5);
                }else {
                    sub = date.substring(0,8)+"0"+date.substring(8);
                }
            }else{
                sub = date.substring(0,5)+"0"+date.substring(5,7)+"0"+date.substring(7);
            }
            return sub.replace("\\u5e74","-").replace("\\u6708","-").replace("\\u65e5","");
        }else if (matcherDate4.find()){
            String sub;
            if (length==8){
                sub = date + "01";
            }else{
                sub = date.substring(0,5)+"0"+date.substring(5)+"01";
            }
            return sub.replace("\\u5e74","-").replace("\\u6708","-");
        } else{
            return date.substring(0,4)+"-"+date.substring(4,6)+"-"+date.substring(6);
        }

    }


    /**
     * 日期匹配
     */
    public static boolean isDate(String date){
        Pattern date1 = Pattern.compile(DATE1);
        Pattern date2 = Pattern.compile(DATE2);
        Pattern date3 = Pattern.compile(DATE3);
        Pattern date4 = Pattern.compile(DATE4);
        Pattern date5 = Pattern.compile(DATE5);
        Matcher matcherDate1 = date1.matcher(date);
        Matcher matcherDate2 = date2.matcher(date);
        Matcher matcherDate3 = date3.matcher(date);
        Matcher matcherDate4 = date4.matcher(date);
        Matcher matcherDate5 = date5.matcher(date);
        return matcherDate1.find() || matcherDate2.find() || matcherDate3.find() || matcherDate4.find() || matcherDate5.find();
    }


    /**
     * 判断权是否为api地址，如：
     *  put:/management/permission/template/**
     *  get:/management/district/all
     * @param apiUrl api url
     * @return boolean
     **/
    public static boolean isApiUrl(String apiUrl) {
        if (StringUtils.isEmpty(apiUrl)) {
            return false;
        }
        String apiUrlRegExp = "^((get)|(post)|(put)|(delete)):/[\\w-]{1,80}(/([\\w-]{1,80}|(\\*\\*))){0,15}$";
        Pattern p = Pattern.compile(apiUrlRegExp);
        Matcher m = p.matcher(apiUrl);
        return m.matches();
    }

}

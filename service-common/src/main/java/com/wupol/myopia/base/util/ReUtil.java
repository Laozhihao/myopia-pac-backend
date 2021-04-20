package com.wupol.myopia.base.util;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则匹配验证
 */
public class ReUtil {
    /**
     *匹配规则
     */
    private static final Pattern CHINESE_XINJIANG_PATTERN =
            Pattern.compile("^[\\u4E00-\\u9FA5\\uf900-\\ufa2d·s]{2,20}$");
    /**
     * 日期匹配规则
     */
    private static final String DATE1 = "^\\d{4}\\-\\d{1,2}\\-\\d{1,2}$";
    private static final String DATE2 = "^\\d{4}\\/\\d{1,2}\\/\\d{1,2}$";
    private static final String DATE3 = "^\\d{4}\\u5e74\\d{1,2}\\u6708\\d{1,2}\\u65e5$";
    private static final String DATE4 = "^\\d{4}\\u5e74\\d{1,2}\\u6708$";
    private static final String DATE5 = "^\\d{8}$";

    /**
     * 学籍号匹配规则
     */
    private static final Pattern STUDENT_NO = Pattern.compile("^[0-9][1-9][0-9][1-9][0-9][1-9][1,2][0-9]{3}[0,1][0-9][0,1,2,3][0-9][0-9]{4}$");

    /**
     * 常见特殊符号匹配规则
     */
    private static final Pattern SPECIAL_STR =Pattern.compile("[`~!@#$%^&*()+=|{}:;\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？']");


    /**
     * 姓名匹配方法
     * @param name
     * @return
     */
    public static boolean isName(String name){

        Matcher m = SPECIAL_STR.matcher(name);
        if (m.find()){

        }else {
            if (name.length()>1){
                return true;
            }
        }
        return false;
    }

    /**
     * 日期匹配
     */
    public static String convertDate(String date){
        int length = date.length();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
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
            String sub = null;
            if (length==10){
                return date.replaceAll("/","-");
            }else if (length==9){
                if (!date.substring(5,6).equals("0")){
                    sub = date.substring(0,5)+"0"+date.substring(5);
                }else {
                    sub = date.substring(0,8)+"0"+date.substring(8);
                }
            }else{
                sub = date.substring(0,5)+"0"+date.substring(5,7)+"0"+date.substring(7);
            }
            return sub.replaceAll("/","-");
        }else if (matcherDate3.find()) {
            String sub = null;
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
            return sub.replaceAll("\\u5e74","-").replaceAll("\\u6708","-").replaceAll("\\u65e5","");
        }else if (matcherDate4.find()){
            String sub = null;
            if (length==8){
                sub = date + "01";
            }else{
                sub = date.substring(0,5)+"0"+date.substring(5)+"01";
            }
            return sub.replaceAll("\\u5e74","-").replaceAll("\\u6708","-");
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
        if (matcherDate1.find()){

        } else if (matcherDate2.find()){

        } else if (matcherDate3.find()){

        }else if (matcherDate4.find()){

        }else if (matcherDate5.find()){

        } else {
            return false;
        }
        return true;

    }


    /**
     * 常见特殊字符过滤
     *
     * @param str
     * @return
     */
    public static boolean filtration(String str) {
        Matcher m = SPECIAL_STR.matcher(str);
        if (m.find()){

        }else {
            return false;
        }
        return true;
    }

    /**
     * StudentNo是否符合规范
     * @return
     */
    public static boolean isValidStudentNo(String str){
        Matcher m = STUDENT_NO.matcher(str);
        if (m.find()){

        }else {
            return false;
        }
        return true;

    }


}

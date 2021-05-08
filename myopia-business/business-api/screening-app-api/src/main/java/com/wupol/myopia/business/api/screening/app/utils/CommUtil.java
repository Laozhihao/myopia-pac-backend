package com.wupol.myopia.business.api.screening.app.utils;

import cn.hutool.core.bean.BeanUtil;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.cglib.beans.BeanMap;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommUtil {
    /**
     * 15位身份证号
     */
    private static final Integer FIFTEEN_ID_CARD=15;
    /**
     * 18位身份证号
     */
    private static final Integer EIGHTEEN_ID_CARD=18;




    public static int sphDegrees(String object) {
        object = object.replace("-", "");
        object = object.replace(" ","");
        double doubleNum = Double.parseDouble(object);
        doubleNum = doubleNum*100;
        String str = doubleNum+"";
        String substring = str.substring(0, 3);
        if (substring.contains(".")){
            substring = str.substring(0, 2);
        }
        return Integer.parseInt(substring);
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public static boolean isNotNull(Object object) {

        if (object==null){
            return false;
        }

        return true;
    }

    public static List<Long> setIds(String ids) {
        String[] split = null;

        if (ids.contains(",")){

            split = ids.split(",");

        }else {
            split = new String[1];

            split[0]  = ids;

        }

        List<Long> idsLong = Arrays.stream(split)
                .map(s ->Long.parseLong(s.trim())).collect(Collectors.toList());

        return idsLong;
    }

    public static <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = Maps.newHashMap();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                map.put(key + "", beanMap.get(key));
            }
        }
        return map;
    }

    /**
     * 是否为手机号
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles) {

        boolean b = Pattern.matches("^1[3|4|5|6|7|8|9][0-9]\\d{4,8}$", mobiles);

        return b;
    }

    /**
     * 判断是否为电话号
     * @param mobiles
     * @return
     */
    public static boolean isPhoneNO(String mobiles) {

        String phone = "0\\d{2,3}-\\d{7,8}";
        Pattern p = Pattern.compile(phone);
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 将通配符表达式转化为正则表达式
     * @param path
     * @return
     */
    private static String getRegPath(String path) {
        char[] chars = path.toCharArray();
        int len = chars.length;
        StringBuilder sb = new StringBuilder();
        boolean preX = false;
        for(int i=0;i<len;i++){
            if (chars[i] == '*'){//遇到*字符
                if (preX){//如果是第二次遇到*，则将**替换成.*
                    sb.append(".*");
                    preX = false;
                }else if(i+1 == len){//如果是遇到单星，且单星是最后一个字符，则直接将*转成[^/]*
                    sb.append("[^/]*");
                }else{//否则单星后面还有字符，则不做任何动作，下一把再做动作
                    preX = true;
                    continue;
                }
            }else{//遇到非*字符
                if (preX){//如果上一把是*，则先把上一把的*对应的[^/]*添进来
                    sb.append("[^/]*");
                    preX = false;
                }
                if (chars[i] == '?'){//接着判断当前字符是不是?，是的话替换成.
                    sb.append('.');
                }else{//不是?的话，则就是普通字符，直接添进来
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 根据身份证号获取出生日期
     * @param certificateNo
     * @return
     */
    public static String getBirthday(String certificateNo) {

        if (certificateNo==null){
            return null;
        }

        String birthday =null;

        char[] number = certificateNo.toCharArray();
        boolean flag = true;
        if (number.length == 15) {
            for (int x = 0; x < number.length; x++) {
                if (!flag)
                    return null;
                flag = Character.isDigit(number[x]);
            }
        } else if (number.length == 18) {
            for (int x = 0; x < number.length - 1; x++) {
                if (!flag)
                    return null;;
                flag = Character.isDigit(number[x]);
            }
        }
        if (flag && certificateNo.length() == 15) {
            birthday = "19" + certificateNo.substring(6, 8) + "-" + certificateNo.substring(8, 10) + "-"
                    + certificateNo.substring(10, 12);
        } else if (flag && certificateNo.length() == 18) {
            birthday = certificateNo.substring(6, 10) + "-" + certificateNo.substring(10, 12) + "-"
                    + certificateNo.substring(12, 14);
        }
        return birthday;
    }


    public static String getSex(String IDCard){
        String sex ="";
        if (StringUtils.isNotBlank(IDCard)){
            //15位身份证号
            if (IDCard.length() == FIFTEEN_ID_CARD){
                if (Integer.parseInt(IDCard.substring(14, 15)) % 2 == 0) {
                    sex = "女";
                } else {
                    sex = "男";
                }
                //18位身份证号
            }else if(IDCard.length() == EIGHTEEN_ID_CARD){
                // 判断性别
                if (Integer.parseInt(IDCard.substring(16).substring(0, 1)) % 2 == 0) {
                    sex = "女";
                } else {
                    sex = "男";
                }
            }
        }
        return sex;
    }


    /**
     * 是否为数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){

        try {
            Double num  = Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isIdCard(String idCard) {
        if (idCard == null || "".equals(idCard)) {
            return false;
        }
        // 定义判别用户身份证号的正则表达式（15位或者18位，最后一位可以为字母）
        String regularExpression = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|" +
                "(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)";
        //假设18位身份证号码:41000119910101123X  410001 19910101 123X
        //^开头
        //[1-9] 第一位1-9中的一个      4
        //\\d{5} 五位数字           10001（前六位省市县地区）
        //(18|19|20)                19（现阶段可能取值范围18xx-20xx年）
        //\\d{2}                    91（年份）
        //((0[1-9])|(10|11|12))     01（月份）
        //(([0-2][1-9])|10|20|30|31)01（日期）
        //\\d{3} 三位数字            123（第十七位奇数代表男，偶数代表女）
        //[0-9Xx] 0123456789Xx其中的一个 X（第十八位为校验值）
        //$结尾

        //假设15位身份证号码:410001910101123  410001 910101 123
        //^开头
        //[1-9] 第一位1-9中的一个      4
        //\\d{5} 五位数字           10001（前六位省市县地区）
        //\\d{2}                    91（年份）
        //((0[1-9])|(10|11|12))     01（月份）
        //(([0-2][1-9])|10|20|30|31)01（日期）
        //\\d{3} 三位数字            123（第十五位奇数代表男，偶数代表女），15位身份证不含X
        //$结尾


        boolean matches = idCard.matches(regularExpression);

        //判断第18位校验值
        if (matches) {

            if (idCard.length() == 18) {
                try {
                    char[] charArray = idCard.toCharArray();
                    //前十七位加权因子
                    int[] idCardWi = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
                    //这是除以11后，可能产生的11位余数对应的验证码
                    String[] idCardY = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
                    int sum = 0;
                    for (int i = 0; i < idCardWi.length; i++) {
                        int current = Integer.parseInt(String.valueOf(charArray[i]));
                        int count = current * idCardWi[i];
                        sum += count;
                    }
                    char idCardLast = charArray[17];
                    int idCardMod = sum % 11;
                    if (idCardY[idCardMod].toUpperCase().equals(String.valueOf(idCardLast).toUpperCase())) {
                        return true;
                    } else {
                        System.out.println("身份证最后一位:" + String.valueOf(idCardLast).toUpperCase() +
                                "错误,正确的应该是:" + idCardY[idCardMod].toUpperCase());
                        return false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("异常:" + idCard);
                    return false;
                }
            }

        }
        return matches;
    }
    public static String getInIds(String ids){
        ids = ids.replace("[","");
        ids = ids.replace("]","");
        return ids;
    }


    public static String updateDeptId(String deptIds) {
        if (!deptIds.contains(",")){
            return "dept_"+deptIds;
        }

        String[] ids = deptIds.split(",");

        String result = "";
        for (String id:ids){
            result = "dept_"+id;
        }
        return result;
    }
    public static String saveDouble2(String data){
        data =  data.replace(" ","");

        //是否为小数
        if (!isDouble(data)){
            return data;
        }

        double num = Double.parseDouble(data);

        DecimalFormat formater = new DecimalFormat();

        formater.setMaximumFractionDigits(2);
        formater.setGroupingSize(0);
        formater.setRoundingMode(RoundingMode.FLOOR);

        String str = formater.format(num);

        return  roundByScale(Double.parseDouble(str),2);
    }
    public static boolean isDouble(String str){

        Pattern pattern = Pattern.compile("^[-//+]?//d+(//.//d*)?|//.//d+$");

        Matcher isNum = pattern.matcher(str);

        if( !isNum.matches() ){
            return false;
        }

        return true;

    }
    public static String roundByScale(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The   scale   must   be   a   positive   integer   or   zero");
        }
        if(scale == 0){
            return new DecimalFormat("0").format(v);
        }
        String formatStr = "0.";
        for(int i=0;i<scale;i++){
            formatStr = formatStr + "0";
        }
        return new DecimalFormat(formatStr).format(v);
    }

    public static List<Long> idsToLongs(String ids) {
        if (ids.contains(",")){
            List<String> lis = Arrays.asList(ids.split(","));
            List<Long> longs = lis.stream().map(x -> Long.parseLong(x)).collect(Collectors.toList());
            return longs;
        }else {
            List<Long> list = new ArrayList<Long>();
            list.add(Long.parseLong(ids));

            return list;
        }
    }

    /**
     * 拷贝属性，但不适用null替换原属性
     * @param source 源
     * @return String[]
     */
    public static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static void copyPropertiesIgnoreNull(Object src, Object target){
        BeanUtil.copyProperties(src, target, getNullPropertyNames(src));
    }


    public static Integer stringToInteger(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        Double doubleData = Double.valueOf(value);
        return (int)Math.ceil(doubleData);
    }
}

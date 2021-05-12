package com.wupol.myopia.business.api.screening.app.utils;

import lombok.extern.log4j.Log4j2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class CommUtil {

    /**
     * 15位身份证号
     */
    private static final Integer FIFTEEN_ID_CARD = 15;
    /**
     * 18位身份证号
     */
    private static final Integer EIGHTEEN_ID_CARD = 18;

    /**
     * 是否为手机号
     * @param mobiles
     * @return
     */
    public static boolean isMobileNO(String mobiles) {
        return Pattern.matches("^1[3|4|5|6|7|8|9][0-9]\\d{4,8}$", mobiles);
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
        if (number.length == FIFTEEN_ID_CARD) {
            for (char aNumber : number) {
                if (!flag)
                    return null;
                flag = Character.isDigit(aNumber);
            }
        } else if (number.length == EIGHTEEN_ID_CARD) {
            for (int x = 0; x < number.length - 1; x++) {
                if (!flag)
                    return null;
                flag = Character.isDigit(number[x]);
            }
        }
        if (flag && certificateNo.length() == FIFTEEN_ID_CARD) {
            birthday = "19" + certificateNo.substring(6, 8) + "-" + certificateNo.substring(8, 10) + "-"
                    + certificateNo.substring(10, 12);
        } else if (flag && certificateNo.length() == EIGHTEEN_ID_CARD) {
            birthday = certificateNo.substring(6, 10) + "-" + certificateNo.substring(10, 12) + "-"
                    + certificateNo.substring(12, 14);
        }
        return birthday;
    }
}

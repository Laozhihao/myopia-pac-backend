package com.wupol.myopia.base.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 正则表达式
 *
 * @author Simple4H
 */
public class RegularUtils {

    // 手机正则
    private static final String REGULAR_MOBILE = "^(1[3-9]([0-9]{9}))$";

    // 身份证正则
    private static final String REGULAR_ID_CARD = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)";

//    private static final String REGULAR_ID_CARD = "^[1-9][0-9]{5}(18|19|20)[0-9]{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)[0-9]{3}([0-9]|([Xx]))";


    /**
     * 校验手机号
     *
     * @param phone 手机号
     * @return boolean true:是  false:否
     */
    public static boolean isMobile(String phone) {
        if (StringUtils.isNotBlank(phone)) {
            return Pattern.matches(REGULAR_MOBILE, phone);
        }
        return false;
    }


    /**
     * 校验身份证
     *
     * @param idCard 身份证号码
     * @return boolean true:是  false:否
     */
    public static boolean isIdCard(String idCard) {
        if (StringUtils.isNotBlank(idCard)) {
            return Pattern.matches(REGULAR_ID_CARD, idCard);
        }
        return false;
    }
}
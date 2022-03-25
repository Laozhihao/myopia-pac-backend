package com.wupol.myopia.base.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 正则表达式
 *
 * @author Simple4H
 */
@UtilityClass
public class RegularUtils {

    /**
     * 手机正则
     */
    public final String REGULAR_MOBILE = "^(1[3-9]([0-9]{9}))$";

    /**
     * 身份证正则
     */
    public final String REGULAR_ID_CARD = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)";

    /**
     * 固定电话正则
     */
    public final String REGULAR_TELEPHONE = "(0\\d{2,3}-[1-9]\\d{5,7})";

    /**
     * 护照正则
     */
    public final String REGULAR_PASSPORT = "/^1[45][0-9]{7}$|(^[P|p|S|s]\\d{7}$)|(^[S|s|G|g|E|e]\\d{8}$)|";

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

    public static boolean isPassport(String passport){
        if (StringUtils.isNotBlank(passport)){
            return Pattern.matches(REGULAR_PASSPORT,passport);
        }
        return false;
    }
}
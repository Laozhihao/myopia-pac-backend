package com.wupol.myopia.business.common.utils.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 脱敏工具
 *
 * @author Simple4H
 */
public class MaskUtil {

    /**
     * 脱敏身份证
     *
     * @param idCard 身份证
     * @return 身份证
     */
    public static String maskIdCard(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return StringUtils.EMPTY;
        }
        return StringUtils.overlay(idCard, getOverlay(9), 5, idCard.length() - 4);
    }

    /**
     * 脱敏护照
     *
     * @param passport 护照
     * @return 护照
     */
    public static String maskPassport(String passport) {
        if (StringUtils.isBlank(passport)) {
            return StringUtils.EMPTY;
        }
        return StringUtils.overlay(passport, getOverlay(3), 2, passport.length() - 1);
    }

    /**
     * 获取占位符
     *
     * @return 占位符
     */
    private static String getOverlay(Integer overlayInt) {
        StringBuilder overlay = new StringBuilder(StringUtils.EMPTY);
        for (int i = 0; i < overlayInt; i++) {
            overlay.append("*");
        }
        return overlay.toString();
    }
}

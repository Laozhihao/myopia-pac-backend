package com.wupol.myopia.business.api.device.util;

import com.wupol.myopia.base.exception.BusinessException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 解析二维码工具类
 *
 * @author Simple4H
 */
@UtilityClass
@Slf4j
public class ParsePlanStudentUtils {

    public static Integer parsePlanStudentId(String uid) {
        try {
            String strUid = parseUid2PlanStudentId(uid);
            return Objects.nonNull(strUid) ? Integer.valueOf(strUid) : Integer.valueOf(uid);
        } catch (Exception e) {
            log.error("用户UID:{}", uid, e);
            throw new BusinessException("二维码解析异常");
        }
    }

    /**
     * 解析二维码
     */
    public static String parseUid2PlanStudentId(String uid) {
        if (uid.startsWith("SA@") || uid.startsWith("SV@")) {
            return uid.substring(uid.indexOf("@") + 1);
        }
        if (uid.startsWith("[VS@")) {
            String s = StringUtils.substringBetween(uid, "@", ",");
            return s.substring(s.indexOf("_") + 1);
        }
        if (uid.startsWith("VS@")) {
            return StringUtils.substringAfterLast(uid, "_");
        }
        return null;
    }
}

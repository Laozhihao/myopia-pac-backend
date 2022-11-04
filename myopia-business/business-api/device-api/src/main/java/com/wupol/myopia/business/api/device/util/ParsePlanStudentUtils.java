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
            Integer uid1 = parseUid2PlanStudentId(uid);
            return Objects.nonNull(uid1) ? uid1 : Integer.valueOf(uid);
        } catch (Exception e) {
            log.error("用户UID:{}", uid, e);
            throw new BusinessException("二维码解析异常");
        }
    }

    /**
     * 解析二维码内容
     * <p>
     * 此方法不会抛出异常。即：就算是解析异常，也会保存原始数据，不会设计到筛查学生的逻辑
     * </p>
     *
     * @param uid uid
     *
     * @return 二维码内容
     */
    public static String parsePlanStudentIdWithoutException(String uid) {
        try {
            Integer uid1 = parseUid2PlanStudentId(uid);
            return Objects.nonNull(uid1) ? String.valueOf(uid1) : StringUtils.EMPTY;
        } catch (Exception e) {
            log.error("用户UID:{}", uid, e);
            return StringUtils.EMPTY;
        }
    }

    private static Integer parseUid2PlanStudentId(String uid) {
        if (uid.startsWith("SA@") || uid.startsWith("SV@")) {
            return Integer.valueOf(uid.substring(uid.indexOf("@") + 1));
        }
        if (uid.startsWith("[VS@")) {
            String s = StringUtils.substringBetween(uid, "@", ",");
            return Integer.valueOf(s.substring(s.indexOf("_") + 1));
        }
        if (uid.startsWith("VS@")) {
            return Integer.valueOf(StringUtils.substringAfterLast(uid, "_"));
        }
        return null;
    }
}

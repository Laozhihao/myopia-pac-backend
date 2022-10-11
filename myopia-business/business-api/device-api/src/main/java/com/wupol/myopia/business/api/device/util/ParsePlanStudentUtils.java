package com.wupol.myopia.business.api.device.util;

import com.wupol.myopia.base.exception.BusinessException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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
            if (uid.startsWith("SA@") || uid.startsWith("SV@")) {
                return Integer.valueOf(uid.substring(uid.indexOf("@") + 1));
            }
            if (uid.startsWith("[VS@")) {
                String s = StringUtils.substringBetween(uid, "@", ",");
                return Integer.valueOf(s.substring(s.indexOf("_") + 1));
            }
            return Integer.valueOf(uid);
        } catch (Exception e) {
            log.error("用户UID:{}", uid);
            throw new BusinessException("二维码解析异常");
        }
    }

    //TODO: 此代码应该放在单元测试里面
    public static void main(String[] args) {
        System.out.println(parsePlanStudentId("[VS@138_166712,166712,FM,25,null,0,null,null,null]"));
    }
}

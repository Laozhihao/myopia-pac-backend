package com.wupol.myopia.business.common.utils.util;

import cn.hutool.core.util.IdcardUtil;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * 身份证工具
 *
 * @author Simple4H
 */
@UtilityClass
public class IdCardUtil {


    /**
     * 通过身份获取性别
     *
     * @param idCard 身份证
     * @return 性别
     */
    public static Integer getGender(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return null;
        }
        int gender = IdcardUtil.getGenderByIdCard(idCard);
        if (gender == 1) {
            return GenderEnum.MALE.type;
        }
        if (gender == 0) {
            return GenderEnum.FEMALE.type;
        }
        return null;
    }

    /**
     * 通过身份获取生日
     *
     * @param idCard 身份证
     * @return 生日
     */
    public static Date getBirthDay(String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return null;
        }
        return IdcardUtil.getBirthDate(idCard);

    }
}

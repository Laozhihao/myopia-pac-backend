package com.wupol.myopia.business.common.utils.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * @Classname PatientAgeUtil
 * @Description 用于计算受检者月龄
 * @Date 2021/7/7 10:16 下午
 * @Author Jacob
 * @Version
 */
//todo 待整理
@UtilityClass
public final class PatientAgeUtil {

    /**
     * 计算年龄段
     * @param patientAge
     * @return
     */
    public Integer getPatientAgeRange(int patientAge) {
        int result = 0;
        if (patientAge <= 12) {
            result = 1;
        } else if (patientAge <= 36) {
            result = 2;
        } else if (patientAge <= 6 * 12) {
            result = 3;
        } else if (patientAge <= 20 * 12) {
            result = 4;
        } else {
            result = 5;
        }
        return result;
    }

    /**
     * 获取年龄范围
     * @param range
     * @return
     */
    public String getAgeRange(int range) {
        String result = StringUtils.EMPTY;
        switch (range) {
            case 1:
                result = "6-12M";
                break;
            case 2:
                result = "1-3Y";
                break;
            case 3:
                result = "3-6Y";
                break;
            case 4:
                result = "6-20Y";
                break;
            case 5:
                result = "20-100Y";
                break;
        }
        return result;
    }

}

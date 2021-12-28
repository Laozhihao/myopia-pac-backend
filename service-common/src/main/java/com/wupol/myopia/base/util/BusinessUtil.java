package com.wupol.myopia.base.util;

import java.util.Date;
import java.util.Objects;

/**
 * @Author wulizhou
 * @Date 2021/12/28 16:04
 */
public class BusinessUtil {

    /**
     * 检验合作信息是否有效
     * @param cooperationType
     * @param cooperationTimeType
     * @param cooperationStartTime
     * @param cooperationEndTime
     * @return
     */
    public static boolean checkCooperation(Integer cooperationType, Integer cooperationTimeType, Date cooperationStartTime,
                                           Date cooperationEndTime) {
        if (Objects.isNull(cooperationType) || Objects.isNull(cooperationTimeType) || Objects.isNull(cooperationStartTime)
                || Objects.isNull(cooperationEndTime)) {
            return false;
        }
        if (cooperationStartTime.getTime() > cooperationEndTime.getTime()) {
            return false;
        }
        return true;
    }

}

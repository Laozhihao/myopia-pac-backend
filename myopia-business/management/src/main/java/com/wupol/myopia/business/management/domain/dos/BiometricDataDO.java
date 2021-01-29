package com.wupol.myopia.business.management.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Description
 * @Date 2021/1/26 1:08
 * @Author by Jacob
 */
@Data
@Accessors(chain = true)
public class BiometricDataDO {

    private BiometricData rightEyeData;
    private BiometricData leftEyeData;

    @Data
    @Accessors(chain = true)
    public static class BiometricData {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * AD
         */
        private String AD;
        /**
         * AL
         */
        private String AL;
        /**
         * CCT
         */
        private String CCT;
        /**
         * LT
         */
        private String LT;
        /**
         * WTW
         */
        private String WTW;
    }

}

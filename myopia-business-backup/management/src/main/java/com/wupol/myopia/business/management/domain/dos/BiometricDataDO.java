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
    public static class BiometricData  {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * AD
         */
        private String ad;
        /**
         眼轴
         */
        private String al;
        /**
         * 角膜中央厚度
         */
        private String cct;
        /**
         * 晶状体厚度
         */
        private String lt;
        /**
         * 角膜白到白距离
         */
        private String wtw;

    }

}

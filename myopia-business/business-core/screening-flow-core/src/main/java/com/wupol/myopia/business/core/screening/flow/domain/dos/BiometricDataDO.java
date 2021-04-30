package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bouncycastle.asn1.x509.qualified.BiometricData;

/**
 * 生物测量DO
 * @Description
 * @Date 2021/1/26 1:08
 * @Author by Jacob
 */
@Data
@Accessors(chain = true)
public class BiometricDataDO {
    /**
     * 右眼数据
     */
    private BiometricData rightEyeData;
    /**
     * 左眼数据
     */
    private BiometricData leftEyeData;

    @Data
    @Accessors(chain = true)
    public static class BiometricData  {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 房水深度
         */
        private String ad;
        /**
         * 眼轴
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

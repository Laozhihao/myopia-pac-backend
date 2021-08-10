package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 生物测量DO
 * @Description
 * @Date 2021/1/26 1:08
 * @Author by Jacob
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class BiometricDataDO implements Serializable {
    /**
     * 右眼数据
     */
    private BiometricData rightEyeData;
    /**
     * 左眼数据
     */
    private BiometricData leftEyeData;
    /**
     * 是否配合检查：0-配合、1-不配合
     */
    private Integer isCooperative;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @Accessors(chain = true)
    public static class BiometricData implements Serializable {
        /**
         * 0 为左眼 1 为右眼
         */
        private Integer lateriality;
        /**
         * 房水深度（前房深度）
         */
        private String ad;
        /**
         * 眼轴（眼轴总长度）
         */
        private String al;
        /**
         * 角膜中央厚度
         */
        private String cct;
        /**
         * 晶状体厚度（晶体厚度）
         */
        private String lt;
        /**
         * 角膜白到白距离（角膜直径）
         */
        private String wtw;
        /**
         * 角膜前表面曲率K1
         */
        private String k1;
        /**
         * 角膜前表面曲率K1的度数
         */
        private String k1Axis;
        /**
         * 角膜前表面曲率K2
         */
        private String k2;
        /**
         * 角膜前表面曲率K2的度数
         */
        private String k2Axis;
        /**
         * 垂直方向角膜散光度数
         */
        private String ast;
        /**
         * 瞳孔直径
         */
        private String pd;
        /**
         * 玻璃体厚度
         */
        private String vt;
    }

}

package com.wupol.myopia.business.api.school.management.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 生物测量
 *
 * @author hang.yuan
 * @date 2022/9/13
 */
@Data
@Accessors(chain = true)
public class BiometricDataVO implements Serializable {

    /**
     * 0 为左眼 1 为右眼
     */
    private Integer eyeType;

    /**
     * 角膜前表面曲率K1
     */
    private String k1;
    /**
     * 角膜前表面曲率K2
     */
    private String k2;

    /**
     * 垂直方向角膜散光度数
     */
    private String ast;
    /**
     * 瞳孔直径
     */
    private String pd;
    /**
     * 角膜白到白距离（角膜直径）
     */
    private String wtw;
    /**
     * 眼轴（眼轴总长度）
     */
    private String al;
    /**
     * 角膜中央厚度
     */
    private String cct;

    /**
     * 房水深度（前房深度）
     */
    private String ad;

    /**
     * 晶状体厚度（晶体厚度）
     */
    private String lt;

    /**
     * 玻璃体厚度
     */
    private String vt;
}
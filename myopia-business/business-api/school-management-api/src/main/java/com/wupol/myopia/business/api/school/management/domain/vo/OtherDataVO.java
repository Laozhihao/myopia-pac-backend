package com.wupol.myopia.business.api.school.management.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 其它
 *
 * @author hang.yuan
 * @date 2022/9/13
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class OtherDataVO implements Serializable {
    /**
     * 0 为左眼 1 为右眼
     */
    private Integer eyeType;
    /**
     * 裂隙灯
     */
    private String slitLamp;

    /**
     * 眼位
     */
    private String ocularInspection;

    /**
     * 眼压
     */
    private String fundus;

    /**
     * 其它眼病
     */
    private String otherEyeDiseases;

    public OtherDataVO(Integer eyeType) {
        this.eyeType = eyeType;
    }
}
package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 串镜检查结果
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CrossMirrorResult {

    /**
     * 0 为左眼 1 为右眼
     */
    private Integer lateriality;

    /**
     * 是否近视
     */
    private Boolean myopia;

    /**
     * 是否远视
     */
    private Boolean farsightedness;
}

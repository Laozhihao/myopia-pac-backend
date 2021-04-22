package com.wupol.myopia.business.core.screening.flow.domain.bo;

import lombok.Getter;
import lombok.Setter;

/**
 * 串镜检查结果
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CrossMirrorResultBO {

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

    /**
     * 是否有其他
     */
    private Boolean other = false;
}

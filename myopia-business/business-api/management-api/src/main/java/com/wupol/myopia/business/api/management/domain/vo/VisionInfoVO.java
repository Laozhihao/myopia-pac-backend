package com.wupol.myopia.business.api.management.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 视力基本信息
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VisionInfoVO {

    /**
     * 近视等级
     */
    private Integer myopiaLevel;

    /**
     * 远视等级
     */
    private Integer farsightednessLevel;

    /**
     * 是否散光
     */
    private Boolean astigmatism;
}
package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 近视情况（性别）
 * @Author wulizhou
 * @Date 2022/12/26 15:42
 */
@Data
@Builder
@Accessors(chain = true)
public class GenderMyopiaInfoDTO {

    /**
     * 维度名称，年级名称/班级名称
     */
    private String name;

    /**
     * 有效筛查人数
     */
    private Long validScreeningNum;

    /**
     * 近视人数
     */
    private Long myopiaNum;

    /**
     * 近视率
     */
    private Float myopiaRatio;

    /**
     * 有效筛查人数（男）
     */
    private Long maleNum;

    /**
     * 近视人数（男）
     */
    private Long maleMyopiaNum;

    /**
     * 近视率（男）
     */
    private Long maleMyopiaRatio;

    /**
     * 有效筛查人数（女）
     */
    private Long femaleNum;

    /**
     * 近视人数（女）
     */
    private Long femaleMyopiaNum;

    /**
     * 近视率（女）
     */
    private Long femaleMyopiaRatio;

}

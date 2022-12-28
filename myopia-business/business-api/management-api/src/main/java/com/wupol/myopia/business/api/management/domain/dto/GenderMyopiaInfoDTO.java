package com.wupol.myopia.business.api.management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 近视情况（性别）
 * @Author wulizhou
 * @Date 2022/12/26 15:42
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GenderMyopiaInfoDTO {

    /**
     * 有效筛查人数
     */
    private int validScreeningNum;

    /**
     * 近视人数
     */
    private int myopiaNum;

    /**
     * 近视率
     */
    private Float myopiaRatio;

    /**
     * 有效筛查人数（男）
     */
    private int maleNum;

    /**
     * 近视人数（男）
     */
    private int maleMyopiaNum;

    /**
     * 近视率（男）
     */
    private Float maleMyopiaRatio;

    /**
     * 有效筛查人数（女）
     */
    private int femaleNum;

    /**
     * 近视人数（女）
     */
    private int femaleMyopiaNum;

    /**
     * 近视率（女）
     */
    private Float femaleMyopiaRatio;

}

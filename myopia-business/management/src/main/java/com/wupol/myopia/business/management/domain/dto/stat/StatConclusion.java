package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Builder;
import lombok.Data;

/**
 * 统计结论
 */
@Data
@Builder
public class StatConclusion {
    /** 是否视力低下 */
    private Boolean isLowVision;

    /** 是否屈光不正 */
    private Boolean isRefractiveError;

    /** 是否近视 */
    private Boolean isMyopia;

    /** 是否远视 */
    private Boolean isHyperopia;

    /** 是否散光 */
    private Boolean isAstigmatism;

    /** 裸眼视力 */
    private Float nakedVision;

    /** 是否戴镜 */
    private Boolean isWearingGlasses;

    /** 数据是否有效 */
    private Boolean isValid;

    /** 预警级别 */
    private Integer warningLevel;
}

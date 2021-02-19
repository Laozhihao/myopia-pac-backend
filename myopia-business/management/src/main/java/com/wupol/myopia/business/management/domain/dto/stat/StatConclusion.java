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

    /** 是否戴镜 */
    private Boolean isWearingGlasses;

    /** 数据是否有效 */
    private Boolean isValid;

    /** 学龄 */
    private Integer schoolAge;

    /** 性别 */
    private Integer gender;

    /** 是否复测 */
    private Integer isRescreen;

    /** 预警级别 */
    private Integer warningLevel;
}

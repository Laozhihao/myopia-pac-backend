package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RescreenStat implements Serializable {
    /** 复测人数 */
    private long rescreenNum;

    /** 戴镜复测人数 */
    private long wearingGlassesRescreenNum;

    /** 戴镜复测指标数 */
    private long wearingGlassesRescreenIndexNum;

    /** 非戴镜复测人数 */
    private long withoutGlassesRescreenNum;

    /** 非戴镜复测指标数 */
    private long withoutGlassesRescreenIndexNum;

    /** 复测项次 */
    private long rescreenItemNum;

    /** 错误项次数 */
    private long incorrectItemNum;

    /** 错误率/发生率 */
    private float incorrectRatio;
}

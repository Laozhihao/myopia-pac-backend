package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RescreenStat {
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

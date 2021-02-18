package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RescreenStat {
    /** 复测人数 */
    private Integer rescreenNum;

    /** 戴镜复测人数 */
    private Integer wearingGlassesRescreenNum;

    /** 戴镜复测指标数 */
    private Integer wearingGlassesRescreenIndexNum;

    /** 非戴镜复测人数 */
    private Integer withoutGlassesRescreenNum;

    /** 非戴镜复测指标数 */
    private Integer withoutGlassesRescreenIndexNum;

    /** 复测项次 */
    private Integer rescreenItemNum;

    /** 错误项次数 */
    private Integer incorrectItemNum;

    /** 错误率/发生率 */
    private Float incorrectRatio;
}

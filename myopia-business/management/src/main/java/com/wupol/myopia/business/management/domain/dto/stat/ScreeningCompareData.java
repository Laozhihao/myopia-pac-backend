package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreeningCompareData {
    /** 筛查学生数 */
    private Integer screeningNum;

    /** 实际筛查学生数 */
    private Integer ActualScreeningNum;

    /** 左眼平均视力 */
    private Integer averageVisionLeft;

    /** 右眼平均视力 */
    private Integer averageVisionRight;

    /** 视力低下率 */
    private Float lowVisionRate;

    /** 视力不良率 */
    private Float poorVisionRate;

    /** 戴镜率 */
    private Float wearingGlassesRate;

    /** 近视人数 */
    private Integer myopiaNum;

    /** 近视率 */
    private Float myopiaRate;

    /** 重点视力对象数量 */
    private Integer focusTargetsNum;

    /** 0级预警率 */
    private Float warningLevelZeroRate;

    /** 1级预警率 */
    private Float warningLevelOneRate;

    /** 2级预警率 */
    private Float warningLevelTwoRate;

    /** 3级预警率 */
    private Float warningLevelThreeRate;

    /** 建议就诊数 */
    private Integer recommendVisitNum;

    /** 视力筛查完成率 */
    private Float screeningFinishedRate;

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
    private Float incorrectRate;
}

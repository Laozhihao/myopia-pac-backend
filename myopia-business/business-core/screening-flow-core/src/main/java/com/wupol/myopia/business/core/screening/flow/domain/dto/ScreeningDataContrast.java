package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreeningDataContrast {
    /** 筛查学生数 */
    private long screeningNum;

    /** 实际筛查学生数 */
    private long actualScreeningNum;

    /** 有效筛查学生数 */
    private long validScreeningNum;

    /** 左眼平均视力 */
    private Float averageVisionLeft;

    /** 右眼平均视力 */
    private Float averageVisionRight;

    /** 视力低下人数 */
    private long lowVisionNum;

    /** 视力低下率 */
    private Float lowVisionRatio;

    /** 屈光不正率 */
    private Float refractiveErrorRatio;

    /** 戴镜人数 */
    private long wearingGlassesNum;

    /** 戴镜率 */
    private Float wearingGlassesRatio;

    /** 近视人数 */
    private long myopiaNum;

    /** 近视率 */
    private Float myopiaRatio;

    /** 重点视力对象数量 */
    private long focusTargetsNum;

    /** 重点视力对象占比 */
    private Float focusTargetsRatio;

    /** 0级预警人数 */
    private long warningLevelZeroNum;

    /** 0级预警率 */
    private Float warningLevelZeroRatio;

    /** 1级预警人数 */
    private long warningLevelOneNum;

    /** 1级预警率 */
    private Float warningLevelOneRatio;

    /** 2级预警人数 */
    private long warningLevelTwoNum;

    /** 2级预警率 */
    private Float warningLevelTwoRatio;

    /** 3级预警人数 */
    private long warningLevelThreeNum;

    /** 3级预警率 */
    private Float warningLevelThreeRatio;

    /** 建议就诊数 */
    private long recommendVisitNum;

    /** 建议就诊占比 */
    private Float recommendVisitRatio;

    /** 视力筛查完成率 */
    private Float screeningFinishedRatio;

    /** 复测数据 */
    private RescreenStat rescreenStat;
}

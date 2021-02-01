package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreeningDataStat {
    /** 筛查学生数 */
    private Integer screeningNum;

    /** 实际筛查学生数 */
    private Integer actualScreeningNum;

    /** 视力筛查完成率 */
    private Float screeningFinishedRatio;

    /** 左眼平均视力 */
    private Float averageVisionLeft;

    /** 右眼平均视力 */
    private Float averageVisionRight;

    /** 视力低下率 */
    private Float lowVisionRatio;
    /** 男视力低下率 */
    private Float lowVisionMenRatio;
    /** 女视力低下率 */
    private Float lowVisionWomenRatio;

    /** 屈光不正率 */
    private Float refractiveErrorRatio;

    /** 近视人数 */
    private Integer myopiaNum;

    /** 近视率 */
    private Float myopiaRatio;

    /** 重点视力对象数量 */
    private Integer focusTargetsNum;

    /** 0级预警率 */
    private Float warningLevelZeroRatio;

    /** 1级预警率 */
    private Float warningLevelOneRatio;

    /** 2级预警率 */
    private Float warningLevelTwoRatio;

    /** 3级预警率 */
    private Float warningLevelThreeRatio;

    /** 建议就诊数 */
    private Integer recommendVisitNum;

    /** 复测人数 */
    private Integer rescreenNum;

    /** 戴镜率 */
    private Float wearingGlassesRatio;

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

    @Data
    public static class BasicStatParams {
        private float ratio;
        private float num;
    }

    @Data
    public static class GenderStat extends BasicStatParams {
        private BasicStatParams male;
        private BasicStatParams female;
    }

    @Data
    public static class SchoolTypeStat extends BasicStatParams {
        private BasicStatParams primary;
        private BasicStatParams secondary;
        private BasicStatParams high;
        private BasicStatParams integratedMiddle;
        private BasicStatParams edu9;
        private BasicStatParams edu12;
        private BasicStatParams vocational;
        private BasicStatParams others;
    }
}

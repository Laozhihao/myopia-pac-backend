package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreeningClassStat {
    /** 筛查通知ID */
    private Integer notificationId;

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
    private ClassStat lowVision;

    /** 屈光不正率 */
    private ClassStat refractiveError;

    /** 戴镜情况 */
    private ClassStat wearingGlasses;

    /** 近视情况 */
    private ClassStat myopia;

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
    private Long rescreenItemNum;

    /** 错误项次数 */
    private Integer incorrectItemNum;

    /** 错误率/发生率 */
    private Float incorrectRatio;

    @Data
    @AllArgsConstructor
    public static class BasicStatParams {
        /** 占比 */
        private float ratio;
        /** 数量 */
        private Integer num;
    }

    @Data
    @AllArgsConstructor
    public static class ClassStat {
        /** 占比 */
        private float ratio;
        /** 数量 */
        private Integer num;
        /** 男性 */
        private BasicStatParams male;
        /** 女性 */
        private BasicStatParams female;
        /** 幼儿园 */
        private BasicStatParams kindergarten;
        /** 小学 */
        private BasicStatParams primary;
        /** 初中 */
        private BasicStatParams junior;
        /** 高中 */
        private BasicStatParams high;
        /** 职业中学 */
        private BasicStatParams vocationalHigh;
    }
}

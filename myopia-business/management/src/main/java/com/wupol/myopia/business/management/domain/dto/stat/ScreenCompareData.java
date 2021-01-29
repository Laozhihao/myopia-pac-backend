package com.wupol.myopia.business.management.domain.dto.stat;

public class ScreenCompareData {
    /** 筛查学生数 */
    private Integer screeningNum;

    /** 实际筛查数量 */
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

    /** 重点实例对象数量 */
    private Integer focusTargetsNum;

    private Integer warningLevelZeroNum;
    private Integer warningLevelOneNum;
    private Integer warningLevelTwoNum;
    private Integer warningLevelThreeNum;

    private Integer recommendNum;
    private Integer visionDoneNum;
    private Integer rescreenNum;
    private Integer wearingGlassesRescreenNum;

    private Float wearingGlassesRescreenRate;
}

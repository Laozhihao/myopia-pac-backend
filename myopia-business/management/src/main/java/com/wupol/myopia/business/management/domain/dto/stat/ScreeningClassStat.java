package com.wupol.myopia.business.management.domain.dto.stat;

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

    /** 有效筛查学生数 */
    private Integer validScreeningNum;

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

    /** 复测数据 */
    private RescreenStat rescreenStat;
}

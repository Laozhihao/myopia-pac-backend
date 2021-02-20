package com.wupol.myopia.business.management.domain.dto.stat;

import java.util.Arrays;
import java.util.List;

import com.wupol.myopia.business.management.domain.model.DistrictAttentiveObjectsStatistic;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 统计预警信息
 */
@Data
@Builder
@Accessors(chain = true)
public class WarningInfo {
    /** 当前统计时间 */
    private Long statTime;

    /** 总重点视力对象数 */
    private Integer focusTargetsNum;

    /** 总视力对象数在总学生数中的占比 */
    private Float focusTargetsPercentage;

    /** 去年今日的时间 */
    private Long lastStatTime;

    /** 去年今日的总重点视力对象数 */
    private Integer lastFocusTargetsNum;

    /** 去年今日的总视力对象数在总学生数中的占比 */
    private Float lastFocusTargetsPercentage;

    /** 分级预警信息 */
    private List<WarningLevelInfo> warningLevelInfoList;

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class WarningLevelInfo {
        /** 预警级别 */
        private Integer warningLevel;

        /** 人数 */
        private Integer num;

        /** 人数比例 */
        private Float percentage;

        public WarningLevelInfo(Integer warningLevel, Integer num, Float percentage) {
            this.warningLevel = warningLevel;
            this.num = num;
            this.percentage = percentage;
        }

        public static List<WarningLevelInfo> getList(DistrictAttentiveObjectsStatistic districtAttentiveObjectsStatistic) {
            WarningLevelInfo warningLevelInfo0 = new WarningLevelInfo();
            warningLevelInfo0.setWarningLevel(0).setNum(districtAttentiveObjectsStatistic.getVisionLabel0Numbers()).setPercentage(districtAttentiveObjectsStatistic.getVisionLabel0Ratio().floatValue());
            WarningLevelInfo warningLevelInfo1 = new WarningLevelInfo();
            warningLevelInfo1.setWarningLevel(1).setNum(districtAttentiveObjectsStatistic.getVisionLabel1Numbers()).setPercentage(districtAttentiveObjectsStatistic.getVisionLabel1Ratio().floatValue());
            WarningLevelInfo warningLevelInfo2 = new WarningLevelInfo();
            warningLevelInfo2.setWarningLevel(2).setNum(districtAttentiveObjectsStatistic.getVisionLabel2Numbers()).setPercentage(districtAttentiveObjectsStatistic.getVisionLabel2Ratio().floatValue());
            WarningLevelInfo warningLevelInfo3 = new WarningLevelInfo();
            warningLevelInfo3.setWarningLevel(3).setNum(districtAttentiveObjectsStatistic.getVisionLabel3Numbers()).setPercentage(districtAttentiveObjectsStatistic.getVisionLabel3Ratio().floatValue());
           return Arrays.asList(warningLevelInfo0,warningLevelInfo1,warningLevelInfo2,warningLevelInfo3);
        }
    }
}

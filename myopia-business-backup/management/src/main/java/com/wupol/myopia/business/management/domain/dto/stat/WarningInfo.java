package com.wupol.myopia.business.management.domain.dto.stat;

import com.wupol.myopia.business.management.domain.model.DistrictAttentiveObjectsStatistic;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.List;

/**
 * 统计预警信息
 */
@Data
@Builder
@Accessors(chain = true)
public class WarningInfo {
    /**
     * 统计起始时间
     */
    private Long statTime;

    /**
     * 统计终止时间
     */
    private Long endTime;

    /**
     * 总重点视力对象数
     */
    private Long focusTargetsNum;

    /**
     * 总视力对象数在总学生数中的占比
     */
    private Float focusTargetsPercentage;

    /**
     * 分级预警信息
     */
    private List<WarningLevelInfo> warningLevelInfoList;

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class WarningLevelInfo {
        /**
         * 预警级别
         */
        private int warningLevel;

        /**
         * 人数
         */
        private long num;

        /**
         * 人数比例
         */
        private Float percentage;

        public WarningLevelInfo(Integer warningLevel, Long num, Float percentage) {
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
            return Arrays.asList(warningLevelInfo0, warningLevelInfo1, warningLevelInfo2, warningLevelInfo3);
        }
    }
}

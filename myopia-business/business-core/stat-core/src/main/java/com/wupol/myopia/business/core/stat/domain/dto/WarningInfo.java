package com.wupol.myopia.business.core.stat.domain.dto;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.core.stat.domain.dos.VisionWarningDO;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

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
    private Integer focusTargetsNum;

    /**
     * 总视力对象数在总学生数中的占比
     */
    private String focusTargetsPercentage;

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
        private Integer warningLevel;

        /**
         * 人数
         */
        private Integer num;

        /**
         * 人数比例
         */
        private String percentage;

        public WarningLevelInfo(Integer warningLevel, Integer num, String percentage) {
            this.warningLevel = warningLevel;
            this.num = num;
            this.percentage = percentage;
        }

        public static List<WarningLevelInfo> getList(VisionWarningDO visionWarning) {
            List<WarningLevelInfo> warningLevelInfoList = Lists.newArrayList();
            if (Objects.nonNull(visionWarning)){
                WarningLevelInfo warningLevelInfo0 = new WarningLevelInfo();
                warningLevelInfo0.setWarningLevel(0).setNum(visionWarning.getVisionLabel0Num()).setPercentage(visionWarning.getVisionLabel0Ratio());
                warningLevelInfoList.add(warningLevelInfo0);
                WarningLevelInfo warningLevelInfo1 = new WarningLevelInfo();
                warningLevelInfo1.setWarningLevel(1).setNum(visionWarning.getVisionLabel1Num()).setPercentage(visionWarning.getVisionLabel1Ratio());
                warningLevelInfoList.add(warningLevelInfo1);
                WarningLevelInfo warningLevelInfo2 = new WarningLevelInfo();
                warningLevelInfo2.setWarningLevel(2).setNum(visionWarning.getVisionLabel2Num()).setPercentage(visionWarning.getVisionLabel2Ratio());
                warningLevelInfoList.add(warningLevelInfo2);
                WarningLevelInfo warningLevelInfo3 = new WarningLevelInfo();
                warningLevelInfo3.setWarningLevel(3).setNum(visionWarning.getVisionLabel3Num()).setPercentage(visionWarning.getVisionLabel3Ratio());
                warningLevelInfoList.add(warningLevelInfo2);

            }
            return warningLevelInfoList;
        }
    }
}

package com.wupol.myopia.business.management.domain.dto.stat;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 统计预警信息
 */
@Data
@Builder
@Accessors(chain = true)
public class WarningInfo {
    /**
     * 当前统计时间
     */
    private Long statTime;

    /**
     * 总重点视力对象数
     */
    private Integer focusTargetsNum;

    /**
     *  总视力对象数在总学生数中的占比
     */
    private Float focusTargetsPercentage;

    /**
     * 去年今日的时间
     */
    private Long lastStatTime;

    /**
     * 去年今日的总重点视力对象数
     */
    private Integer lastFocusTargetsNum;

    /**
     *  去年今日的总视力对象数在总学生数中的占比
     */
    private Float lastFocusTargetsPercentage;

    /**
     * 分级预警信息
     */
    private List<WarningLevelInfo> warningLevelInfoList;

    @Data
    @Accessors(chain = true)
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
        private Float percentage;

        public WarningLevelInfo(Integer warningLevel, Integer num, Float percentage) {
            this.warningLevel = warningLevel;
            this.num = num;
            this.percentage = percentage;
        }
    }
}

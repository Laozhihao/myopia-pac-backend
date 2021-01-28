package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 统计预警信息
 */
@Data
@Accessors(chain = true)
public class StudentGradeWarning {
    /**
     * 当前统计时间
     */
    private Long statTime;

    /**
     * 总重点视力对象数
     */
    private Long keyObjNum;

    /**
     *  总视力对象数在总学生数中的占比
     */
    private Float keyObjPercentage;

    /**
     * 去年今日的时间
     */
    private Long lastStatTime;

    /**
     * 去年今日的总重点视力对象数
     */
    private Long lastKeyObjNum;

    /**
     *  去年今日的总视力对象数在总学生数中的占比
     */
    private Long lastKeyObjPercentage;

    /**
     * 分级预警信息
     */
    private WarningLevelInfo warningLevelInfo;

    @Data
    public static class WarningLevelInfo {
        /**
         * 预警级别
         */
        private Integer warningLevel;

        /**
         * 人数
         */
        private Long num;

        /**
         * 人数比例
         */
        private Float rate;
    }
}

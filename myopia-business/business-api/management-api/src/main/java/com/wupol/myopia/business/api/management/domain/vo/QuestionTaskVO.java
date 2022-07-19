package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 筛查任务
 *
 * @author xz 2022 07 06 12:30
 */
@Data
public class QuestionTaskVO {
    /**
     * 年度
     */
    private String annual;

    /**
     * 筛查任务
     */
    private List<Item> tasks;

    @Data
    public static class Item{
        /**
         * 筛查标题
         */
        private String taskTitle;

        /**
         * 筛查时间段 开始
         */
        private Date screeningStartTime;

        /**
         * 筛查时间段 结束
         */
        private Date screeningEndTime;

        /**
         * 筛查任务Id
         */
        private Integer taskId;

        /**
         * 创建时间
         */
        private Date createTime;
    }
}

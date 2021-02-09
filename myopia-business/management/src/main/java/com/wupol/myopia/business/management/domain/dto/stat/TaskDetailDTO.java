package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Data;

import java.util.List;

@Data
public class TaskDetailDTO {
    /**
     * 年度
     */
    private String year;

    /**
     * 下级的数据列表，如果没有的话，为null
     */
    private List<Item> content;

    @Data
    public static class Item {
        /**
         * 任务名
         */
        private Long taskName;

        /**
         * 任务id
         */
        private Long taskId;
        /**
         * 开始时间
         */
        private Long screeningStartTime;
        /**
         * 结束时间
         */
        private Long screeningEndTime;
    }


}

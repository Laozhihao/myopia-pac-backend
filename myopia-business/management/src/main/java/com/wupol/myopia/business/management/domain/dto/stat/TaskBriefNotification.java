package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Data;

@Data
public class TaskBriefNotification {
    /** 通知ID */
    private Integer id;

    /** 通知标题 */
    private String title;

    /** 开始时间 */
    private Long startTime;

    /** 结束时间 */
    private Long endTime;

    public TaskBriefNotification(Integer id, String title, Long startTime, Long endTime) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}

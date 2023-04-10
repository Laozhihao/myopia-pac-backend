package com.wupol.myopia.business.common.utils.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 关联通知
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class LinkNoticeQueue {

    /**
     * 唯一键
     */
    private String uniqueId;

    /**
     * 区域Id
     */
    private Integer districtId;

    /**
     * 任务Id
     */
    private Integer screeningTaskId;

    /**
     * 通知Id
     */
    private Integer screeningNoticeId;

    /**
     * 计划Id
     */
    private Integer planId;

    /**
     * 创建人Id
     */
    private Integer createUserId;
}

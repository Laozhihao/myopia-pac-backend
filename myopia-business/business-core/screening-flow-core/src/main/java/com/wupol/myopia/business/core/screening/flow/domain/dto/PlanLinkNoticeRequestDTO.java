package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 计划关联通知
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PlanLinkNoticeRequestDTO {

    /**
     * 通知Id
     */
    private Integer srcScreeningNoticeId;

    /**
     * taskId
     */
    private Integer screeningTaskId;
}

package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 计划关联通知
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PlanLinkNoticeRequestDTO {

    /**
     * 计划Id
     */
    @NotNull(message = "计划Id不能为空")
    private Integer planId;

    /**
     * 任务Id
     */
    @NotNull(message = "任务Id不能为空")
    private Integer screeningTaskId;

    /**
     * 通知机构Id
     */
    @NotNull(message = "通知机构Id不能为空")
    private Integer screeningNoticeDeptOrgId;
}

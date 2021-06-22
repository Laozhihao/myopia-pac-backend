package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 筛查任务Vo
 *
 * @author Alix
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class ScreeningPlanNameDTO {
    /**
     * 筛查任务开始时间
     */
    private Date screeningStartTime;
    /**
     * 筛查任务结束时间
     */
    private Date screeningEndTime;
    /**
     * 计划id
     */
    private Integer planId;
    /**
     * 计划名称
     */
    private String planName;
    /**
     * 创建时间
     */
    private Date createTime;
}
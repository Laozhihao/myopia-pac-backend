package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 筛查端-记录详情
 *
 * @author Simple4H
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ScreeningOrgPlanResponseDTO extends ScreeningPlan {

    private ScreeningRecordItems items;

    /**
     * 筛查状态 0-未开始 1-进行中 2-已结束
     */
    private Integer screeningStatus;
}

package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import lombok.Getter;
import lombok.Setter;

/**
 * 筛查端-记录详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScreeningOrgPlanResponse extends ScreeningPlan {

    private ScreeningRecordItems items;

    /**
     * 筛查状态 0-未开始 1-进行中 2-已结束
     */
    private Integer screeningStatus;
}

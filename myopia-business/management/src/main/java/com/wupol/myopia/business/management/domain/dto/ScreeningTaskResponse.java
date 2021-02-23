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
public class ScreeningTaskResponse extends ScreeningPlan {

    private ScreeningRecordItems items;
}

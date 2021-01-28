package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.SchoolVisionStatistic;
import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import lombok.Getter;
import lombok.Setter;

/**
 * 学校端-筛查记录
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScreeningPlanResponse extends ScreeningPlan {

    /**
     * 详情
     */
    private SchoolVisionStatistic items;

    /**
     * 机构名称
     */
    private String orgName;

}

package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 学校端-筛查记录
 *
 * @author Simple4H
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ScreeningPlanResponseDTO extends ScreeningPlan {

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 详情
     */
    private List<SchoolVisionStatisticItem> items;

    /**
     * 是否能关联通知
     */
    private Boolean isCanLink;

}

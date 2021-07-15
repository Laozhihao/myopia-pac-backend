package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 筛查计划Vo
 * @author Alix
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class ScreeningPlanDTO extends ScreeningPlan {
    /**
     * 筛查计划中的学校列表
     */
    private List<ScreeningPlanSchool> schools;
}
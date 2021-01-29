package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchool;
import lombok.Data;

import java.util.List;

/**
 * 筛查计划新增/更新的数据结构
 * @author Alix
 */
@Data
public class ScreeningPlanDTO extends ScreeningPlan {
    /**
     * 筛查计划中的学校列表
     */
    List<ScreeningPlanSchool> schools;
}

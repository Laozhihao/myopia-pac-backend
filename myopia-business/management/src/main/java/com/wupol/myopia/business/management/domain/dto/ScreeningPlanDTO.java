package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchool;
import lombok.Data;

/**
 * @Description 用于筛查计划  和 筛查计划关联的学校的联表
 * @Date 2021/1/25 16:35
 * @Author by Jacob
 */
@Data
public class ScreeningPlanDTO {
    private ScreeningPlan screeningPlan;
}

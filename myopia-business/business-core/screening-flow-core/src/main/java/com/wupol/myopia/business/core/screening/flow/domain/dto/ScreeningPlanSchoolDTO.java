package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 筛查计划学校
 * @author Alix
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class ScreeningPlanSchoolDTO extends ScreeningPlanSchool {
    /** 筛查学生数 */
    private Integer studentCount;
}
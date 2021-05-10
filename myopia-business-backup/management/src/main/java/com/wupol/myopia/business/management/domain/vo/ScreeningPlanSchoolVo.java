package com.wupol.myopia.business.management.domain.vo;

import com.wupol.myopia.business.management.domain.model.ScreeningPlanSchool;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 筛查计划学校
 * @author Alix
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class ScreeningPlanSchoolVo extends ScreeningPlanSchool {
    /** 筛查学生数 */
    private Integer studentCount;
}
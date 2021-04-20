package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 筛查计划Vo
 * @author Alix
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class ScreeningPlanDTO extends ScreeningPlan {
    /** 行政区域名 */
    private String creatorName;
    /** 行政区域名称 */
    private String districtName;
    /** 部门名称 */
    private String govDeptName;
    /** 筛查机构名称 */
    private String screeningOrgName;
}
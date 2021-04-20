package com.wupol.myopia.business.management.domain.vo;

import com.wupol.myopia.business.management.domain.model.ScreeningPlan;
import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 筛查计划Vo
 * @author Alix
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class ScreeningPlanVo extends ScreeningPlan {
    /** 行政区域名 */
    private String creatorName;
    /** 行政区域名称 */
    private String districtName;
    /** 部门名称 */
    private String govDeptName;
    /** 筛查机构名称 */
    private String screeningOrgName;
}
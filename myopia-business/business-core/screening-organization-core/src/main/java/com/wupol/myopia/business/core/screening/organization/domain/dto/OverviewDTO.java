package com.wupol.myopia.business.core.screening.organization.domain.dto;

import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2022/2/22 18:25
 */
@Data
@Accessors(chain = true)
public class OverviewDTO extends Overview {

    /**
     * 已绑定的医院数量
     */
    private Integer hospitalNum;

    /**
     * 已绑定的筛查机构数量
     */
    private Integer screeningOrganizationNum;

}

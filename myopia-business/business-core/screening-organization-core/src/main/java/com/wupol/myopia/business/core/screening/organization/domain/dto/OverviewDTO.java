package com.wupol.myopia.business.core.screening.organization.domain.dto;

import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author wulizhou
 * @Date 2022/2/22 18:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OverviewDTO extends Overview {

    /**
     * 行政区域名
     */
    private String districtName;

    /**
     * 已绑定的医院数量
     */
    private Long hospitalNum;

    /**
     * 已绑定的筛查机构数量
     */
    private Long screeningOrganizationNum;

}

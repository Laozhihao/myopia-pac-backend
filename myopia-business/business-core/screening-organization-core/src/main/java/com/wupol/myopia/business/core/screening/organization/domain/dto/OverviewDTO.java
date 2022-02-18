package com.wupol.myopia.business.core.screening.organization.domain.dto;

import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import lombok.Data;

import java.util.List;

/**
 * @Author wulizhou
 * @Date 2022/2/18 16:09
 */
@Data
public class OverviewDTO extends Overview {

    /**
     * 绑定医院ids
     */
    private List<Integer> hospitalIds;
    /**
     * 绑定筛查机构ids
     */
    private List<Integer> screeningOrganizationIds;

}

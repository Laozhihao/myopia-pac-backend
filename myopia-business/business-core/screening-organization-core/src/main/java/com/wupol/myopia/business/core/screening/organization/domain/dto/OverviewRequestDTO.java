package com.wupol.myopia.business.core.screening.organization.domain.dto;

import com.wupol.myopia.business.core.screening.organization.domain.model.Overview;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author wulizhou
 * @Date 2022/2/18 16:09
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OverviewRequestDTO extends Overview {

    /**
     * 绑定医院ids
     */
    private List<Integer> hospitalIds;
    /**
     * 绑定筛查机构ids
     */
    private List<Integer> screeningOrganizationIds;

    /**
     * 绑定学校ids
     */
    private List<Integer> schoolIds;

    /**
     * 配置类型List
     */
    private List<String> configTypeList;

}

package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 筛查机构筛查统计
 *
 * @author Simple4H
 */
@Getter
@Setter
public class OrgScreeningCountDTO {

    /**
     * 筛查次数
     */
    private Integer count;

    /**
     * 机构ID
     */
    private Integer screeningOrgId;
}

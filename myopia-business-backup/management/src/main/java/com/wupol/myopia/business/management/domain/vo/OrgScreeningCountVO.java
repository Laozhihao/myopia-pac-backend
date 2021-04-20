package com.wupol.myopia.business.management.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 筛查机构筛查统计
 *
 * @author Simple4H
 */
@Getter
@Setter
public class OrgScreeningCountVO {

    /**
     * 筛查次数
     */
    private Integer count;

    /**
     * 机构ID
     */
    private Integer screeningOrgId;
}

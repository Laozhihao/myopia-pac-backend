package com.wupol.myopia.business.core.common.domain.dto;

import lombok.Data;

/**
 * 筛查机构账号
 *
 * @author Simple4H
 */
@Data
public class OrgAccountListDTO {

    private Integer userId;

    private Integer orgId;

    private String username;

    private Integer status;

}

package com.wupol.myopia.migrate.domain.dto;

import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author HaoHao
 * @Date 2022/3/24
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningOrgDTO extends ScreeningOrganization {
    /**
     * 旧数据ID
     **/
    private String oldOrgId;
}

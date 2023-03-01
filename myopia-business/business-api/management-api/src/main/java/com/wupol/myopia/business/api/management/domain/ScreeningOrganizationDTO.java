package com.wupol.myopia.business.api.management.domain;

import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author 钓猫的小鱼
 * @Date 2023/2/17 16:22
 * @Description
 **/
@Data
public class ScreeningOrganizationDTO extends ScreeningOrganization {
    /**
     * 模板ID
     */
    private Integer templateId;
}

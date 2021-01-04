package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 筛查机构返回体
 *
 * @author Simple4H
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningOrgResponse extends ScreeningOrganization {

    private Integer staffCount;

    private Integer screeningTime;
}

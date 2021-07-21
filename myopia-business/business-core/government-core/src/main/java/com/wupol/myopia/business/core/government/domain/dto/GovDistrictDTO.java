package com.wupol.myopia.business.core.government.domain.dto;

import com.wupol.myopia.business.core.government.domain.model.GovDept;
import lombok.Getter;
import lombok.Setter;

/**
 * 政府行政区域
 *
 * @author Simple4H
 */
@Getter
@Setter
public class GovDistrictDTO extends GovDept {

    /**
     * 行政code
     */
    private Long code;
}

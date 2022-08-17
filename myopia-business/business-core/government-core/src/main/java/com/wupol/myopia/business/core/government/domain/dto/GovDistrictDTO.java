package com.wupol.myopia.business.core.government.domain.dto;

import com.wupol.myopia.business.core.government.domain.model.GovDept;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 政府行政区域
 *
 * @author Simple4H
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GovDistrictDTO extends GovDept {

    /**
     * 行政code
     */
    private Long code;
}

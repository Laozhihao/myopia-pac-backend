package com.wupol.myopia.business.core.government.domain.dto;

import com.wupol.myopia.business.core.government.domain.model.District;
import com.wupol.myopia.business.core.government.domain.model.GovDept;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 政府部门, 显示
 *
 * @Author Chikong
 * @Date 2020/12/22
 **/

@Data
@Accessors(chain = true)
public class GovDeptDTO extends GovDept {
    /** 行政区域 */
    private District district;
}
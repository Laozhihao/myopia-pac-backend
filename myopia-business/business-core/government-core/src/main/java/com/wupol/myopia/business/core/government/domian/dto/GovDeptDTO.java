package com.wupol.myopia.business.core.government.domian.dto;

import com.wupol.myopia.business.core.government.domian.model.District;
import com.wupol.myopia.business.core.government.domian.model.GovDept;
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
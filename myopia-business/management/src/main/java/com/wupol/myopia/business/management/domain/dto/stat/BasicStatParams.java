package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BasicStatParams {
    /** 占比 */
    private float ratio;
    /** 数量 */
    private Integer num;
}

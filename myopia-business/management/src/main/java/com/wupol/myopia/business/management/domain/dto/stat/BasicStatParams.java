package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BasicStatParams {
    /** 统计项名称 */
    private String title;
    /** 占比 */
    private Float ratio;
    /** 数量 */
    private Integer num;
}

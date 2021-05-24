package com.wupol.myopia.business.core.screening.flow.domain.dto;

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
    private Long num;
}

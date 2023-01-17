package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 计划及对应数据量
 * @Author wulizhou
 * @Date 2023/1/12 17:40
 */
@Data
@Accessors(chain = true)
public class ScreeningPlanCount {

    /**
     * 筛查计划id
     */
    private Integer planId;

    /**
     * 对应数据量
     */
    private Integer count;

}

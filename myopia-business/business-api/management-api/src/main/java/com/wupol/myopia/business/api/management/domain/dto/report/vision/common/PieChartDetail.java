package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 饼图详情
 *
 * @author Simple4H
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PieChartDetail {

    /**
     * 描述
     */
    private String name;

    /**
     * 百分比
     */
    private String proportion;

}

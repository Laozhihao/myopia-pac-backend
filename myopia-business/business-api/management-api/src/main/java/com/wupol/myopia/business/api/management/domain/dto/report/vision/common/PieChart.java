package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 饼图
 *
 * @author Simple4H
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PieChart {

    /**
     * 百分比
     */
    private String proportion;

    /**
     * 详情
     */
    private List<PieChartDetail> detail;


}

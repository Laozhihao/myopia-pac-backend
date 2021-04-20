package com.wupol.myopia.business.core.parent.domian.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 筛查报告统计详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScreeningDetail {

    /**
     * 总数
     */
    private Long total;

    /**
     * 详情
     */
    private List<CountReportItems> items;
}

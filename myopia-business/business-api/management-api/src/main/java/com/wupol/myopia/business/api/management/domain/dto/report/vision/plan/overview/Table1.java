package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.overview;

import lombok.Data;

/**
 * 本次筛查对象分布
 *
 * @author Simple4H
 */
@Data
public class Table1 {

    /**
     * 描述
     */
    private String desc;

    /**
     * 男性
     */
    private Long male;

    /**
     * 女性
     */
    private Long female;

    /**
     * 合计
     */
    private Long total;

}

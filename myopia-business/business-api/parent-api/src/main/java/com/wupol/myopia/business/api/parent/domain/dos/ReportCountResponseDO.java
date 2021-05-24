package com.wupol.myopia.business.api.parent.domain.dos;

import lombok.Getter;
import lombok.Setter;

/**
 * 家长端-孩子报告统计
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ReportCountResponseDO {

    /**
     * 学生名称
     */
    private String name;


    /**
     * 筛查详情
     */
    private ScreeningDetailDO screeningDetailDO;


    /**
     * 就诊详情
     */
    private VisitsDetailDO visitsDetailDO;
}

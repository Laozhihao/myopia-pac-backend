package com.wupol.myopia.business.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 报告统计详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CountReportItems {

    /**
     * id
     */
    private Integer id;

    /**
     * 创建时间
     */
    private Date createTime;
}

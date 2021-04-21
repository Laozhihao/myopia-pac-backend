package com.wupol.myopia.business.core.parent.domain.dto;

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
public class CountReportItemsDTO {

    /**
     * id
     */
    private Integer id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}

package com.wupol.myopia.business.core.screening.organization.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 总览机构筛查机构关联表
 *
 * @Author wulizhou
 * @Date 2022-02-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_overview_screening_organization")
public class OverviewScreeningOrganization implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总览机构id
     */
    private Integer overviewId;

    /**
     * 筛查机构id
     */
    private Integer screeningOrganizationId;

    /**
     * 创建时间
     */
    private Date createTime;


}

package com.wupol.myopia.business.core.screening.organization.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 总览机构医院关联表
 *
 * @Author wulizhou
 * @Date 2022-02-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_overview_hospital")
public class OverviewHospital implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总览机构id
     */
    private Integer overviewId;

    /**
     * 医院id
     */
    private Integer hospitalId;

    /**
     * 创建时间
     */
    private Date createTime;


}

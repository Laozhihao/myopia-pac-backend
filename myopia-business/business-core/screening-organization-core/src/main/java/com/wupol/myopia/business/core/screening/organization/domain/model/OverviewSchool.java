package com.wupol.myopia.business.core.screening.organization.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 总览机构学校关联表
 *
 * @author Simple4H
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_overview_school")
public class OverviewSchool implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总览机构id
     */
    private Integer overviewId;

    /**
     * 学校id
     */
    private Integer schoolId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}

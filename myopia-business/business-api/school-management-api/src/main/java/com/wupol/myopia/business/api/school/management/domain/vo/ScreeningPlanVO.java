package com.wupol.myopia.business.api.school.management.domain.vo;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 筛查计划信息
 *
 * @author hang.yuan 2022/9/16 20:12
 */
@Data
public class ScreeningPlanVO implements Serializable {

    /**
     * 筛查计划--标题
     */
    private String title;

    /**
     * 筛查计划--开始时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    private Date startTime;
    /**
     * 筛查计划--结束时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    private Date endTime;

    /**
     * 筛查机构名称
     */
    private String screeningOrgName;

    /**
     * 筛查类型（0：视力筛查，1；常见病）
     */
    private Integer screeningType;

    /**
     * 筛查业务类型（0：协助筛查，1:自主筛查）
     */
    private Integer screeningBizType;
}

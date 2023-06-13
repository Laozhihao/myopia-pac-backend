package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 学龄段统计
 *
 * @author Simple4H
 */
@Data
public class SchoolAgeCount {

    /**
     * 描述
     */
    private String desc;

    /**
     * 统计
     */
    private Long count;

    /**
     * 排序
     */
    @JsonIgnore
    private Integer sort;

    /**
     * 学龄段
     */
    @JsonIgnore
    private Integer schoolAge;
}

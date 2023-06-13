package com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 年级统计
 *
 * @author Simple4H
 */
@Data
public class GradeCodeCount {

    /**
     * 描述
     */
    private String desc;

    /**
     * 排序
     */
    @JsonIgnore
    private Integer sort;

    /**
     * 学龄段
     */
    @JsonIgnore
    private String code;
}

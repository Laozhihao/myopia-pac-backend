package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 筛查端-记录详情
 *
 * @author Simple4H
 */
@Getter
@Setter
public class RecordDetails {

    private Integer schoolId;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 计划的学生数量
     */
    private Integer planScreeningNumbers;

    /**
     * 实际筛查的学生数量
     */
    private Integer realScreeningNumbers;
}

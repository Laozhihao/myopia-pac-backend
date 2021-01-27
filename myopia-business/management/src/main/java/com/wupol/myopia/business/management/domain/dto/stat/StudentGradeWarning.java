package com.wupol.myopia.business.management.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 统计预警信息
 */
@Data
@Accessors(chain = true)
public class StudentGradeWarning {
    /**
     * 预警级别
     */
    private Integer warngingLevel;

    /**
     * 人数
     */
    private Long counts;

    /**
     * 人数比例
     */
    private Float rate;
}

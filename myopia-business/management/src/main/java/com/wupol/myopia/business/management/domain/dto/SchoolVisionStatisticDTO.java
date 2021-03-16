package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.SchoolVisionStatistic;
import lombok.Getter;
import lombok.Setter;

/**
 * 学校筛查计划
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolVisionStatisticDTO extends SchoolVisionStatistic {

    /**
     * 纳入统计的实际筛查数
     */
    private Integer includeScreeningNumbers;
}

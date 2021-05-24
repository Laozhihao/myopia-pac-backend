package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 学校筛查统计
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolScreeningCountDTO {

    /**
     * 筛查次数
     */
    private Integer count;

    /**
     * 学校ID
     */
    private Integer schoolId;
}

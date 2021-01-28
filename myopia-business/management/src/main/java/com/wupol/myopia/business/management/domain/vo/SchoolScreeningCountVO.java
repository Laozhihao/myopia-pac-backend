package com.wupol.myopia.business.management.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 学校筛查统计
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SchoolScreeningCountVO {

    /**
     * 筛查次数
     */
    private Integer count;

    /**
     * 学校ID
     */
    private Integer schoolId;
}

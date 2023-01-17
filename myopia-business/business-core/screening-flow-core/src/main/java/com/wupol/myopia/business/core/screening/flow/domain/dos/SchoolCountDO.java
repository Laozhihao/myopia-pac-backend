package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;

/**
 * 学校统计
 */
@Data
public class SchoolCountDO {
    /**
     * 学校ID
     */
    private Integer schoolId;
    /**
     * 当前学校的相关指标总数
     */
    private Integer schoolCount;
}

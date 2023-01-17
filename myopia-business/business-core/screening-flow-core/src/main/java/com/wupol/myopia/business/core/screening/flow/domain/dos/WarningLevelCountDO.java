package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;

/**
 * 预警人群数量统计
 */
@Data
public class WarningLevelCountDO {
    /**
     * 预警级别
     */
    private Integer warningLevel;
    /**
     * 当前预警级别总数
     */
    private Integer count;
}

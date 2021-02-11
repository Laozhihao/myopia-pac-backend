package com.wupol.myopia.business.management.domain.dto.stat;

import lombok.Data;

/**
 * @Description
 * @Date 2021/2/8 16:57
 * @Author by Jacob
 */
@Data
class ScreeningBasicResult {
    /**
     * 筛查标题
     */
    private String title;
    /**
     * 筛查开始时间
     */
    private Long screeningStartTime;
    /**
     * 筛查结束时间
     */
    private Long screeningEndTime;
    /**
     * 当前统计时间
     */
    private Long statisticTime;

}

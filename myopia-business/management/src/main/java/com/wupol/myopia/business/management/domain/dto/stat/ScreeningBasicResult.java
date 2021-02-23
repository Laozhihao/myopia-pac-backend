package com.wupol.myopia.business.management.domain.dto.stat;

import com.wupol.myopia.business.management.domain.model.ScreeningTask;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Description
 * @Date 2021/2/8 16:57
 * @Author by Jacob
 */
@Getter
@EqualsAndHashCode
@Accessors(chain = true)
class ScreeningBasicResult {
    /**
     * 筛查标题
     */
    private String title;
    /**
     * 筛查开始时间
     */
    private Date screeningStartTime;
    /**
     * 筛查结束时间
     */
    private Date screeningEndTime;
    /**
     * 当前统计时间
     */
    private Date statisticTime;

    public void setDataByScreeningTask(ScreeningTask screeningTask)  {
        this.title = screeningTask.getTitle();
        this.screeningEndTime = screeningTask.getEndTime();
        this.screeningStartTime = screeningTask.getStartTime();
        this.statisticTime = new Date();//todo 先这样，实际上是前晚的11点
    }
}

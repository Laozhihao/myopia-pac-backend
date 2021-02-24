package com.wupol.myopia.business.management.domain.dto.stat;

import com.wupol.myopia.business.management.domain.model.ScreeningNotice;
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

    public void setDataByScreeningNotice(ScreeningNotice screeningNotice)  {
        this.title = screeningNotice.getTitle();
        this.screeningEndTime = screeningNotice.getEndTime();
        this.screeningStartTime = screeningNotice.getStartTime();
    }
}

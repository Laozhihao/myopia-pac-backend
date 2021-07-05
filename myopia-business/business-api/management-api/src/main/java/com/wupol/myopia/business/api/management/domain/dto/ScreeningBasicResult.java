package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Objects;

/**
 * @Description
 * @Date 2021/2/8 16:57
 * @Author by Jacob
 */
@Getter
@EqualsAndHashCode
@Accessors(chain = true)
public class ScreeningBasicResult {
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
     * 通知id
     */
    private Integer screeningNoticeId;

    /**
     * 设置noticeId
     *
     * @param screeningNotice
     */
    public void setDataByScreeningNotice(ScreeningNotice screeningNotice) {
        if (Objects.isNull(screeningNotice)) {
            return ;
        }
        this.title = screeningNotice.getTitle();
        this.screeningNoticeId = screeningNotice.getId();
        this.screeningEndTime = screeningNotice.getEndTime();
        this.screeningStartTime = screeningNotice.getStartTime();
    }
}

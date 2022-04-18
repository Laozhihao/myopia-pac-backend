package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 筛查任务Vo
 *
 * @author Alix
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class ScreeningNoticeNameDTO {
    /**
     * 筛查任务开始时间
     */
    private Date screeningStartTime;
    /**
     * 筛查任务结束时间
     */
    private Date screeningEndTime;
    /**
     * 通知id
     */
    private Integer noticeId;
    /**
     * 通知标题
     */
    private String noticeTitle;

    /**
     * 筛查类型
     */
    private Integer screeningType;
}
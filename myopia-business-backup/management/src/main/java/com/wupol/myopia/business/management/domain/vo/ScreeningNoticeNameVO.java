package com.wupol.myopia.business.management.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class ScreeningNoticeNameVO {
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
}
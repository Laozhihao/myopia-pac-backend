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
public class ScreeningTaskNameVO{
    /**
     * 筛查任务开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date screeningStartTime;
    /**
     * 筛查任务结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date screeningEndTime;
    /**
     * 任务id
     */
    private Integer taskId;
    /**
     * 任务名称
     */
    private String taskName;
}
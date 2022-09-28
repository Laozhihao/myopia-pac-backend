package com.wupol.myopia.business.api.school.management.domain.vo;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 筛查通知列表
 *
 * @author hang.yuan 2022/9/26 15:53
 */
@Data
@Accessors(chain = true)
public class ScreeningNoticeListVO {

    /**
     * 通知id
     */
    private Integer id;

    /**
     * 筛查通知标题
     */
    private String title;

    /**
     * 筛查通知内容
     */
    private String content;

    /**
     * 筛查通知开始时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    private Date startTime;

    /**
     * 筛查通知结束时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    private Date endTime;

    /**
     * 状态(2 未创建 3已创建)
     */
    private Integer status;

    /**
     * 是否可以创建计划
     */
    private Boolean canCreatePlan;

    /**
     * 接收时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_MINUTE_PATTERN)
    private Date acceptTime;

    /**
     * 通知部门名称
     */
    private String noticeDeptName;

    /**
     * 筛查类型（0：视力筛查，1；常见病）
     */
    private Integer screeningType;
}

package com.wupol.myopia.business.core.screening.flow.domain.dto;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.common.utils.domain.model.NotificationConfig;
import lombok.Data;

import java.util.Date;

/**
 * 视力筛查列表
 *
 * @author Simple4H
 */
@Data
public class ScreeningListResponseDTO {

    /**
     * 学校统计Id
     */
    private Integer schoolStatisticId;

    /**
     * 筛查机构Id
     */
    private Integer screeningOrgId;

    /**
     * 筛查机构类型
     */
    private Integer screeningOrgType;

    /**
     * 筛查计划Id
     */
    private Integer planId;

    /**
     * 学校Id
     */
    private Integer schoolId;

    /**
     * 筛查标题
     */
    private String title;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN,timezone = "GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN,timezone = "GMT+8")
    private Date endTime;

    /**
     * 发布状态 （0未发布 1已发布）
     */
    private Integer releaseStatus;

    /**
     * 筛查状态 0-未开始 1-进行中 2-已结束
     */
    private Integer screeningStatus;

    /**
     * 计划的学生数量
     */
    private Integer planScreeningNumbers;

    /**
     * 实际筛查的学生数量
     */
    private Integer realScreeningNumbers;

    /**
     * 筛查机构名
     */
    private String screeningOrgName;

    /**
     * 内容
     */
    private String content;

    /**
     * 发布时间
     */
    private Date releaseTime;

    /**
     * 筛查机构-告知书配置
     */
    private NotificationConfig notificationConfig;

    /**
     * 二维码文件地址
     */
    private String qrCodeFileUrl;

    /**
     * 二维码配置, 英文逗号分隔, 1-普通二维码, 2-vs666, 3-虚拟二维码
     */
    private String qrCodeConfig;
}

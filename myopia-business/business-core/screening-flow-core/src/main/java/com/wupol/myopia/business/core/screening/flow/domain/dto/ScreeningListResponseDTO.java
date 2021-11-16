package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.common.utils.domain.model.NotificationConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 视力筛查列表
 *
 * @author Simple4H
 */
@Getter
@Setter
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
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 发布状态 （0未发布 1已发布）
     */
    private Integer releaseStatus;

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
}

package com.wupol.myopia.business.management.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganization;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 筛查机构返回体
 *
 * @author Simple4H
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ScreeningOrgResponseDTO extends ScreeningOrganization {
    /**
     * 是否能更新
     */
    private Boolean canUpdate = false;

    /**
     * 上次统计时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastCountDate;

    /**
     * 行政区域名
     */
    private String districtName;

    /**
     * 是否已有任务
     */
    private Boolean alreadyHaveTask = false;

    /**
     * 地址详情
     */
    private String addressDetail;
}
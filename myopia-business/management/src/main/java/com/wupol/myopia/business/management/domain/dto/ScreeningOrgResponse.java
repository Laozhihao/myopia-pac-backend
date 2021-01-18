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
public class ScreeningOrgResponse extends ScreeningOrganization {

    /**
     * 筛查人员统计
     */
    private Integer staffCount;

    /**
     * 筛查次数
     */
    private Integer screeningTime;

    /**
     * 是否能更新
     */
    private Boolean canUpdate = false;


    /**
     * 上次统计时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastCountDate;
}

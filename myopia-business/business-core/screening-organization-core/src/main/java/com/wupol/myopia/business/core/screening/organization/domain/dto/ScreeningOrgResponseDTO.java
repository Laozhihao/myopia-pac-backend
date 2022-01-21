package com.wupol.myopia.business.core.screening.organization.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 筛查机构返回体
 *
 * @author Simple4H
 */
@Accessors(chain = true)
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    /**
     * 账号
     */
    private String username;

    /**
     * 是否回显账号
     */
    private boolean displayUsername = false;

    /**
     * 合作医院个数
     */
    private Integer countCooperationHospital;

    /**
     * 该机构下筛查人员总人数
     */
    private Integer screeningStaffTotalNum;
}

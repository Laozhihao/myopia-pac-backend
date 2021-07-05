package com.wupol.myopia.business.core.device.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 设备报告VO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class DeviceReportTemplateVO {

    /**
     * 筛查机构Id
     */
    private Integer screeningOrgId;

    /**
     * 模板类型 1-VS666模板1
     */
    private Integer templateType;
}

package com.wupol.myopia.business.core.device.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 配置机构模版请求DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
public class ConfigurationReportRequestDTO {

    /**
     * 筛查机构Id
     */
    private Integer screeningOrgId;

    /**
     * 模板Id
     */
    private Integer templateId;

    public ConfigurationReportRequestDTO(Integer screeningOrgId, Integer templateId) {
        this.screeningOrgId = screeningOrgId;
        this.templateId = templateId;
    }
}

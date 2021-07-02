package com.wupol.myopia.business.core.device.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

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
    @NotNull(message = "筛查机构Id不能为空")
    private Integer screeningOrgId;

    /**
     * 模板Id
     */
    @NotNull(message = "模板Id不能为空")
    private Integer templateId;

    public ConfigurationReportRequestDTO(Integer screeningOrgId, Integer templateId) {
        this.screeningOrgId = screeningOrgId;
        this.templateId = templateId;
    }
}

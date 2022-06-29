package com.wupol.myopia.business.core.system.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 省份绑定DTO
 *
 * @author Simple4H
 */
@Data
public class TemplateBindRequestDTO {

    private Integer templateId;

    private List<TemplateBindItemDTO> districtInfo;
}

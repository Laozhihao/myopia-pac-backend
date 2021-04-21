package com.wupol.myopia.business.core.system.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 省份绑定DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class TemplateBindRequestDTO {

    private Integer templateId;

    private List<TemplateBindItemDTO> districtInfo;
}

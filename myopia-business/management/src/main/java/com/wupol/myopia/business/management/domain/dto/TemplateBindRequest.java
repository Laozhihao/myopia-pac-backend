package com.wupol.myopia.business.management.domain.dto;

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
public class TemplateBindRequest {

    private Integer templateId;

    private List<TemplateBindItem> districtInfo;
}

package com.wupol.myopia.business.core.system.domain.dto;

import com.wupol.myopia.business.core.system.domain.model.TemplateDistrict;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 模版通用返回体
 *
 * @author Simple4H
 */
@Setter
@Getter
public class TemplateResponseDTO {

    /**
     * id
     */
    private Integer id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 使用的身份
     */
    private List<TemplateDistrict> districtInfo;
}

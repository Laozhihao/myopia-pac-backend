package com.wupol.myopia.business.core.device.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 机构/医院/学校DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceOrgListResponseDTO {

    /**
     * Id
     */
    private Integer orgId;

    /**
     * 名称
     */
    private String name;
}

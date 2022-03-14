package com.wupol.myopia.business.aggregation.screening.domain.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 灯箱数据上传DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class DeviceDataRequestDTO {

    @NotBlank(message = "设备编码不能为空")
    private String deviceSn;

    @NotNull(message = "业务类型不能为空")
    private Integer businessType;

    @NotBlank(message = "数据不能为空")
    private String data;
}

package com.wupol.myopia.business.api.device.domain.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * TODO:
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScalesRequestDTO {

    @NotBlank(message = "事件类型不能为空")
    private String action;

    @NotBlank(message = "设备Id不能为空")
    private String deviceID;

    private List<ScalesData> datas;
}

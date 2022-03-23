package com.wupol.myopia.business.api.device.domain.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * UID获取学生信息
 *
 * @author Simple4H
 */
@Getter
@Setter
public class UserInfoRequestDTO {

    @NotBlank(message = "设备编码不能为空")
    private String deviceSn;

    @NotBlank(message = "UID不能为空")
    private String uid;
}

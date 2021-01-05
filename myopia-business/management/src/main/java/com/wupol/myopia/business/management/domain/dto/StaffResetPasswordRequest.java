package com.wupol.myopia.business.management.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 筛查人员重置密码
 *
 * @author Simple4H
 */
@Data
public class StaffResetPasswordRequest {

    @NotNull(message = "员工id不能为空")
    private Integer staffId;

    @NotBlank(message = "手机号码不能为空")
    private String phone;

    @NotBlank(message = "身份证不能空")
    private String idCard;
}

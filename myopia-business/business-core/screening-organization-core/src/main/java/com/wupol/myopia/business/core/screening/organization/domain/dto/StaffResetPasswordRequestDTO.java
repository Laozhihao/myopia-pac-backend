package com.wupol.myopia.business.core.screening.organization.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 筛查人员重置密码
 *
 * @author Simple4H
 */
@Data
@NoArgsConstructor
public class StaffResetPasswordRequestDTO {

    @NotNull(message = "员工id不能为空")
    private Integer staffId;

    @NotBlank(message = "手机号码不能为空")
    private String phone;

    @NotBlank(message = "身份证不能空")
    private String idCard;

    public StaffResetPasswordRequestDTO(@NotNull(message = "员工id不能为空") Integer staffId, @NotBlank(message = "手机号码不能为空") String phone, @NotBlank(message = "身份证不能空") String idCard) {
        this.staffId = staffId;
        this.phone = phone;
        this.idCard = idCard;
    }
}

package com.wupol.myopia.business.common.utils.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 重置密码
 *
 * @author Simple4H
 */
@Data
public class ResetPasswordRequest {

    @NotNull(message = "id不能为空")
    private Integer id;
}

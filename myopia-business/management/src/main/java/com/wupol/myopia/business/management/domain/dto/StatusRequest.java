package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 更新状态实体类
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StatusRequest {

    /**
     * id
     */
    @NotNull(message = "id不能为空")
    private Integer id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}

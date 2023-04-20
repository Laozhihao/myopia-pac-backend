package com.wupol.myopia.business.api.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * 护眼宝绑定回调
 *
 * @author Simple4H
 */
@Getter
@Setter
public class HybCallbackRequestDTO {

    /**
     * 家长UID
     */
    @NotBlank(message = "家长UID不能为空")
    private String parentUid;
}

package com.wupol.myopia.business.api.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

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
    private String parentUid;
}

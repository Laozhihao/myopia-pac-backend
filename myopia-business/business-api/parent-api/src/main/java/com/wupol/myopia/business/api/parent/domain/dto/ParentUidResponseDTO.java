package com.wupol.myopia.business.api.parent.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 获取家长Uid
 *
 * @author Simple4H
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParentUidResponseDTO {

    /**
     * uid
     */
    private String uid;

    /**
     * jsapiTicket
     */
    private String jsapiTicket;
}

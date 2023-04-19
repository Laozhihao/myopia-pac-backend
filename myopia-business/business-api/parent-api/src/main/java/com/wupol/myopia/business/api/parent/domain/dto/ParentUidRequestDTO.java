package com.wupol.myopia.business.api.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 获取家长Uid
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ParentUidRequestDTO {

    /**
     * id
     */
    private Integer id;

    /**
     * noncestr
     */
    private String noncestr;

    /**
     * timestamp
     */
    private Long timestamp;

    /**
     * url
     */
    private String url;
}

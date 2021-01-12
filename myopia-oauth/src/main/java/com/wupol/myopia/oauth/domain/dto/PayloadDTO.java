package com.wupol.myopia.oauth.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayloadDTO {

    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 用户属于哪个平台的角色类型
     */
    private Integer roleType;
}

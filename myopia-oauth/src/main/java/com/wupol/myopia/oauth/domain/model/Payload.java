package com.wupol.myopia.oauth.domain.model;

import lombok.Data;

@Data
public class Payload {

    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 用户属于哪个平台的角色类型
     */
    private Integer roleType;
    /**
     * 是否管理员
     */
    private Boolean isAdmin;
}

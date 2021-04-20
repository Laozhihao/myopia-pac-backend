package com.wupol.myopia.business.common.utils.domain.dto;

import lombok.Data;

/**
 * 账号密码
 *
 * @author Simple4H
 */
@Data
public class UsernameAndPasswordDTO {

    private String username;

    private String password;

    public UsernameAndPasswordDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

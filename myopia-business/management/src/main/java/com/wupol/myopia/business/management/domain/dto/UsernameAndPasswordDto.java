package com.wupol.myopia.business.management.domain.dto;

import lombok.Data;

/**
 * 账号密码
 *
 * @author Simple4H
 */
@Data
public class UsernameAndPasswordDto {

    private String username;

    private String password;

    public UsernameAndPasswordDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

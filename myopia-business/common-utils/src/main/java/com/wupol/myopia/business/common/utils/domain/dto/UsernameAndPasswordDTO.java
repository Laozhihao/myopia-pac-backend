package com.wupol.myopia.business.common.utils.domain.dto;

import lombok.Data;

/**
 * 账号密码
 *
 * @author Simple4H
 */
@Data
public class UsernameAndPasswordDTO {

    private boolean display;

    private String username;

    private String password;

    public UsernameAndPasswordDTO(String username, String password) {
        this.username = username;
        this.password = password;
        this.display = true;
    }

    public UsernameAndPasswordDTO setNoDisplay() {
        display = false;
        username = null;
        password = null;
        return this;
    }

}

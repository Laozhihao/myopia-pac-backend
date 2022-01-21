package com.wupol.myopia.business.common.utils.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账号密码
 *
 * @author Simple4H
 */
@NoArgsConstructor
@Data
public class UsernameAndPasswordDTO {

    private boolean display;

    private String username;

    private String password;

    private String realName;

    public UsernameAndPasswordDTO(String username, String password) {
        this.username = username;
        this.password = password;
        this.display = true;
    }

    public UsernameAndPasswordDTO(String username, String password, String realName) {
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.display = true;
    }

    public UsernameAndPasswordDTO setNoDisplay() {
        display = false;
        username = null;
        password = null;
        realName = null;
        return this;
    }

}

package com.wupol.myopia.oauth.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 由于受Oauth2机制的限制，下面字段无法使用驼峰法命名
 *
 * @author Bain
 */
@Data
@Accessors(chain = true)
public class LoginDTO {

    /**
     * 用户名
     **/
    private String username;

    /**
     * 用户密码
     **/
    private String password;

    /**
     * 客户端ID
     **/
    private String client_id;

    /**
     * 客户端秘钥
     **/
    private String client_secret;

    /**
     * 用户类型[0 学生；1 学校]
     */
    private String user_type;

    /**
     * 授权类型
     **/
    private String grant_type;

    /**
     * 刷新token
     **/
    private String refresh_token;

    /**
     * 验证标志
     */
    private String verify;

}

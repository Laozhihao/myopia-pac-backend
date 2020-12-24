package com.wupol.myopia.oauth.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@Data
@Builder
public class Oauth2Token {
    /**
     * 访问令牌
     **/
    private String token;
    /**
     * 刷新令牌
     **/
    private String refreshToken;
    /**
     * 有效时间(秒)
     **/
    private int expiresIn;
}

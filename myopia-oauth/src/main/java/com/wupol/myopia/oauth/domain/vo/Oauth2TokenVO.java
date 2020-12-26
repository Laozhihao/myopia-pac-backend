package com.wupol.myopia.oauth.domain.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@Data
@Builder
public class Oauth2TokenVO {
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

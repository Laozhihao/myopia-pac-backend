package com.wupol.myopia.oauth.sdk.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenInfo {
    /**
     * 访问令牌
     **/
    private String accessToken;
    /**
     * 刷新令牌
     **/
    private String refreshToken;
    /**
     * 有效时间(秒)
     **/
    private Integer expiresIn;
}

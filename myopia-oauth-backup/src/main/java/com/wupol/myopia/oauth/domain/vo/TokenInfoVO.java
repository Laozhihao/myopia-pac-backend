package com.wupol.myopia.oauth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@Data
@AllArgsConstructor
public class TokenInfoVO {
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

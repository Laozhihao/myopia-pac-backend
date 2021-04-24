package com.wupol.myopia.oauth.domain.vo;

import com.wupol.myopia.oauth.domain.model.Permission;
import lombok.Data;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.util.List;

/**
 * @Author HaoHao
 * @Date 2021/1/13
 **/
@Data
public class LoginInfoVO {
    /**
     * 令牌信息
     **/
    private TokenInfoVO tokenInfo;
    /**
     * 菜单权限
     **/
    private List<Permission> permissions;

    public LoginInfoVO(OAuth2AccessToken oAuthToken, List<Permission> permissions) {
        this.tokenInfo = new TokenInfoVO(oAuthToken.getValue(), oAuthToken.getRefreshToken().getValue(), oAuthToken.getExpiresIn());
        this.permissions = permissions;
    }
}

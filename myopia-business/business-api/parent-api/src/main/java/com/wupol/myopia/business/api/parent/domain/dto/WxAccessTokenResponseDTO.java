package com.wupol.myopia.business.api.parent.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * AccessToken返回体
 *
 * @author Simple4H
 */
@Getter
@Setter
public class WxAccessTokenResponseDTO extends WxCommonResponseDTO implements Serializable {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;
}

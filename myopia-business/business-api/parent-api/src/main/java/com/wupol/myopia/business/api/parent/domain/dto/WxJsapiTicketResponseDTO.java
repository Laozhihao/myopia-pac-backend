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
public class WxJsapiTicketResponseDTO extends WxCommonResponseDTO implements Serializable {

    private String ticket;

    @JsonProperty("expires_in")
    private Integer expiresIn;
}

package com.wupol.myopia.oauth.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Bain
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class LoginDTO {

    private String username;

    private String password;

    private String client_id;

    private String client_secret;

    private String grant_type;

    private String refresh_token;

}

package com.wupol.myopia.oauth.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Bain
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class LoginDTO {

    private String username;

    @NotEmpty(message = "password is required")
    private String password;

    @NotNull(message = "client_id is required")
    private String client_id;

    @NotNull(message = "client_secret is required")
    private String client_secret;

    @NotNull(message = "grant_type is required")
    private String grant_type;

    private String refresh_token;

}

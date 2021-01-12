package com.wupol.myopia.oauth.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.security.Principal;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Bain
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class LoginDTO implements Principal {

    @NotEmpty(message = "username is required")
    private String username;

    @NotEmpty(message = "credentials is required")
    private String password;

    @NotNull(message = "client_id is required")
    @JsonProperty("client_id")
    private String clientId;

    @NotNull(message = "client_secret is required")
    @JsonProperty("client_secret")
    private String clientSecret;

    private Boolean isDebug;

    @Override
    public String getName() {
        return this.username;
    }
}

package com.wupol.myopia.oauth.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author Bain
 */
@Data
@Accessors(chain = true)
public class LoginDTO {

    private String username;
    @NotEmpty(message = "credentials is required")
    private String password;
    @NotNull(message = "system is required")
    private Integer system;
    private Boolean isDebug;
}

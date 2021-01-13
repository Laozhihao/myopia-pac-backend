package com.wupol.myopia.oauth.domain.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginInfoDTO {
    private String accessToken;
    private UserInfo userInfo;
    private List<String> permissions;

    @Data
    @Builder
    public static class UserInfo {
        private Integer userId;
        private Integer roleType;
    }
}

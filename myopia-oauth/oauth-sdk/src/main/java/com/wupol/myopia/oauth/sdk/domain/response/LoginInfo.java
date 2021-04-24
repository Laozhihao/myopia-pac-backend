package com.wupol.myopia.oauth.sdk.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @Author HaoHao
 * @Date 2021/1/13
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class LoginInfo {
    /**
     * 令牌信息
     **/
    private TokenInfo tokenInfo;
}

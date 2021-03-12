package com.wupol.myopia.business.management.domain.dto.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @Author HaoHao
 * @Date 2021/1/13
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class LoginInfoDTO {
    /**
     * 令牌信息
     **/
    private TokenInfoDTO tokenInfo;
}

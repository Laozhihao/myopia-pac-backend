package com.wupol.myopia.oauth.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author HaoHao
 * @Date 2021/1/13
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class RefreshTokenDTO extends LoginDTO {
    /**
     * 刷新token
     **/
    private String refresh_token;

}

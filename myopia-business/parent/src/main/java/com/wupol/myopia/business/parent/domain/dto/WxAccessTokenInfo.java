package com.wupol.myopia.business.parent.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author HaoHao
 * @Date 2021/3/1
 **/
@Accessors(chain = true)
@Data
public class WxAccessTokenInfo {
    private String openId;
    private String accessToken;
}

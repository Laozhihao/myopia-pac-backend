package com.wupol.myopia.business.core.parent.domian.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 微信授权信息
 * 
 * @Author HaoHao
 * @Date 2021/3/1
 **/
@Accessors(chain = true)
@Data
public class WxAuthorizationInfo {
    /**
     * 公众号的唯一标识
     **/
    private String openId;
    /**
     * 网页授权接口调用凭证
     **/
    private String accessToken;
}

package com.wupol.myopia.business.core.parent.domian.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

/**
 * 微信用户基本信息
 *
 * @Author HaoHao
 * @Date 2021/3/1
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WxUserInfo {

    /**
     * 用户的唯一标识
     **/
    @JsonSetter("openid")
    private String openId;

    /**
     * 用户昵称
     **/
    private String nickname;

    /**
     * 性别，值为1时是男性，值为2时是女性，值为0时是未知
     **/
    private Integer sex;

    /**
     * 用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空。
     * 若用户更换头像，原有头像URL将失效。
     **/
    @JsonSetter("headimgurl")
    private String headImgUrl;
}

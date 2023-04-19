package com.wupol.myopia.business.api.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 微信返回公共体
 *
 * @author Simple4H
 */
@Getter
@Setter
public class WxCommonResponseDTO implements Serializable {

    private Integer errcode;

    private String errmsg;
}

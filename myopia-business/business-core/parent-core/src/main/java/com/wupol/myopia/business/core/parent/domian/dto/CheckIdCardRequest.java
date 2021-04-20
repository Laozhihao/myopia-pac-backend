package com.wupol.myopia.business.core.parent.domian.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 身份证校验请求
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CheckIdCardRequest {

    /**
     * 身份证
     */
    private String idCard;

    /**
     * 姓名
     */
    private String name;


}

package com.wupol.myopia.business.core.parent.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 身份证校验请求
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CheckIdCardRequestDTO {

    /**
     * 身份证
     */
    private String idCard;

    /**
     * 姓名
     */
    private String name;


}

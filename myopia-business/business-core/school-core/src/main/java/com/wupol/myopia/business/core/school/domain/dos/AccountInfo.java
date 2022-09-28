package com.wupol.myopia.business.core.school.domain.dos;

import com.wupol.myopia.base.constant.SystemCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 用户信息
 *
 * @author Simple4H
 */
@Getter
@Setter
public class AccountInfo implements Serializable {

    /**
     * user表Id
     */
    private Integer userId;

    /**
     * 系统类型
     *
     * @see SystemCode
     */
    private Integer systemCode;
}

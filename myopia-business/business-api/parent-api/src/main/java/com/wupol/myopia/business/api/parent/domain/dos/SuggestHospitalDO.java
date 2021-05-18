package com.wupol.myopia.business.api.parent.domain.dos;

import lombok.Getter;
import lombok.Setter;

/**
 * 建议医院
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SuggestHospitalDO {

    /**
     * 头像
     */
    private String avatarFile;

    /**
     * 名称
     */
    private String name;

    /**
     * 地址
     */
    private String address;
}

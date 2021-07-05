package com.wupol.myopia.business.core.common.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 建议医院
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SuggestHospitalDTO {

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

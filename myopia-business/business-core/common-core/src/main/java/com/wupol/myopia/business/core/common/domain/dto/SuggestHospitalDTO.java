package com.wupol.myopia.business.core.common.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 建议医院
 *
 * @author Simple4H
 */
@Getter
@Setter
public class SuggestHospitalDTO implements Serializable {

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

    /**
     * 固定电话
     */
    private String telephone;
}

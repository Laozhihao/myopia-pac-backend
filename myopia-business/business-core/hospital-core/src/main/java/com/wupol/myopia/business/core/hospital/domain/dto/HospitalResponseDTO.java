package com.wupol.myopia.business.core.hospital.domain.dto;

import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import lombok.Getter;
import lombok.Setter;

/**
 * 医院返回题
 *
 * @author Simple4H
 */
@Getter
@Setter
public class HospitalResponseDTO extends Hospital {

    /**
     * 行政区域名
     */
    private String districtName;

    /**
     * 详细地址
     */
    private String addressDetail;

    /**
     * 账号
     */
    private String username;

    /**
     * 是否回显账号
     */
    private boolean displayUsername = false;

    /**
     * 头像Url
     */
    private String avatarUrl;
}

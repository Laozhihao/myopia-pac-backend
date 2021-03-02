package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.Hospital;
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
     * 是否重置密码
     */
    private Boolean updatePassword = false;

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}

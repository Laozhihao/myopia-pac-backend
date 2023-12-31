package com.wupol.myopia.business.core.hospital.domain.dto;

import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 医院返回题
 *
 * @author Simple4H
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = false)
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

    /**
     * 是否已经添加合作医院 true-是 false-否
     */
    private Boolean isAdd = false;

    /**
     * 关联筛查机构的名称
     */
    private String associateScreeningOrgName;

    /**
     * 该医院下的医生总人数
     */
    private Integer doctorTotalNum;
}

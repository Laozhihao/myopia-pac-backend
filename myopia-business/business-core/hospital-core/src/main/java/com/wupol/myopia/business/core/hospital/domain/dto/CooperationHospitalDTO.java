package com.wupol.myopia.business.core.hospital.domain.dto;

import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import lombok.Getter;
import lombok.Setter;

/**
 * 筛查端合作医院DTO
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CooperationHospitalDTO extends Hospital {


    /**
     * 医院Id
     */
    private Integer hospitalId;

    /**
     * 筛查机构Id
     */

    private Integer screeningOrgId;
    /**
     * 行政区域
     */
    private String districtName;

    /**
     * 地址
     */
    private String addressDetail;

    /**
     * 是否置顶
     */
    private Integer isTop;
}

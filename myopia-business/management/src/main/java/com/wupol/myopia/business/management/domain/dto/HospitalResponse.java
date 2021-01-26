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
public class HospitalResponse extends Hospital {

    /**
     * 行政区域名
     */
    private String districtName;
}

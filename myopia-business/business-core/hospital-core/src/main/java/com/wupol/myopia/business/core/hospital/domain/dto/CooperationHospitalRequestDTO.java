package com.wupol.myopia.business.core.hospital.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 请求参数
 *
 * @author Simple4H
 */
@Getter
@Setter
public class CooperationHospitalRequestDTO {

    /**
     * 筛查机构Id
     */
    private Integer screeningOrgId;

    /**
     * 医院Ids
     */
    private List<Integer> hospitalIds;
}

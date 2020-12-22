package com.wupol.myopia.business.management.domain.query;


import com.wupol.myopia.business.management.domain.model.Hospital;
import com.wupol.myopia.business.management.domain.model.ScreeningOrganizationStaff;
import lombok.Data;

/**
 * 医院查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@Data
public class HospitalQuery extends Hospital {
    /** 名称 */
    private String nameLike;
    /** 地区编码 */
    private String code;
}

package com.wupol.myopia.business.core.hospital.domian.query;


import com.wupol.myopia.business.core.hospital.domian.model.Hospital;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 医院查询
 *
 * @Author Chikong
 * @Date 2020-12-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HospitalQuery extends Hospital {
    /** 名称 */
    private String nameLike;
    /** 编号 */
    private String noLike;
    /** 地区编码 */
    private Long code;
}

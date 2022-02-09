package com.wupol.myopia.business.core.hospital.domain.query;


import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

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

    /**
     * 过期时间大于expireDayGt天
     */
    private Integer expireDayGt;
    /**
     * 过期时间小于等于expireDayLe天
     */
    private Integer expireDayLe;

    /**
     * 过期时间大于等于cooperationEndTimeGe
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date cooperationEndTimeGe;

    /**
     * 过期时间小于等于cooperationEndTimeLe
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date cooperationEndTimeLe;
}

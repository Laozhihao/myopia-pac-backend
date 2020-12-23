package com.wupol.myopia.business.management.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 医院列表请求体
 *
 * @author Simple4H
 */
@Getter
@Setter
public class HospitalListRequest {

    /**
     * 医院名称
     */
    private String name;

    /**
     * 医院类型 0-定点医院 1-非定点医院
     */
    private Integer type;

    /**
     * 等级 0-一甲,1-一乙,2-一丙,3-二甲,4-二乙,5-二丙,6-三特,7-三甲,8-三乙,9-三丙 10-其他
     */
    private Integer level;

    /**
     * 医院性质 0-公立 1-私立
     */
    private Integer kind;

    /**
     * 市/区代码
     */
    private Integer code;

    /**
     * 页数
     */
    private Integer current;

    /**
     * 页码
     */
    private Integer size;
}

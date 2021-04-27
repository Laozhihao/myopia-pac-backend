package com.wupol.myopia.business.core.stat.domain.dto;

import lombok.Data;

/**
 * @Description
 * @Date 2021/3/18 15:06
 * @Author by Jacob
 */
@Data
public class BigScreenStatDataDTO {

    /**
     * 学龄
     */
    private Integer schoolAge;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 地区id
     */
    private Integer districtId;
    /**
     * 城市名
     */
    private String cityDistrictName;
    /**
     * 是否视力低下
     */
    private Boolean isLowVision;
    /**
     * 是否屈光不正
     */
    private Boolean isRefractiveError;
    /**
     * 是否近视
     */
    private Boolean isMyopia;
    /**
     * 预警等级
     */
    private Integer warningLevel;
    /**
     * 左眼视力
     */
    private Float visionL;
    /**
     * 右眼视力
     */
    private Float visionR;
    /**
     * 城市地区id
     */
    private Integer cityDistrictId;

    /** 是否有效数据 */
    private Boolean isValid;
}

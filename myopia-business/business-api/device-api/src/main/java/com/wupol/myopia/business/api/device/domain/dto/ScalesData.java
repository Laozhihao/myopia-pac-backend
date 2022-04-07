package com.wupol.myopia.business.api.device.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 体重信息
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScalesData {

    /**
     * 测量用户 ID
     */
    @JsonProperty("UID")
    private String uid;

    /**
     * 用户名称
     */
    private String name;

    /**
     * 年龄
     */
    private String age;

    /**
     * 性别 1=男;2=女;0=未知
     */
    private String sex;

    /**
     * 身份证图片(选配)
     */
    private String headImgStr;

    /**
     * 测量时间戳
     */
    private String occurTime;

    /**
     * 身高体重结果
     */
    @JsonProperty("BMI")
    private BmiData bmi;
}

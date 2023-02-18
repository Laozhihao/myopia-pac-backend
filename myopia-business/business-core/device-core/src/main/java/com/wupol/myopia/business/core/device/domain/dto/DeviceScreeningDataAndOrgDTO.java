package com.wupol.myopia.business.core.device.domain.dto;

import com.wupol.myopia.business.core.device.domain.model.DeviceScreeningData;
import lombok.Data;
import lombok.ToString;

/**
 * @Author wulizhou
 * @Date 2021/6/29 11:49
 */
@Data
@ToString(callSuper = true)
public class DeviceScreeningDataAndOrgDTO extends DeviceScreeningData {
    /**
     * 模板类型 1-VS550 标准模板 2-VS550（0.25D）分辨率 3-VS550（0.01D）分辨率
     */
    private Integer templateType;
    /**
     *
     */
    private String screeningOrgName;

    /**
     * 左眼球镜-展示使用
     */
    private Double leftSphDisplay;

    /**
     * 右眼球镜-展示使用
     */
    private Double rightSphDisplay;

    /**
     * 左眼柱镜-展示使用
     */
    private Double leftCylDisplay;

    /**
     * 右眼柱镜-展示使用
     */
    private Double rightCylDisplay;
    /**
     * 左眼等效球镜
     */
    private Double leftSEDisplay;
    /**
     * 右眼等效球镜
     */
    private Double rightSEDisplay;
}

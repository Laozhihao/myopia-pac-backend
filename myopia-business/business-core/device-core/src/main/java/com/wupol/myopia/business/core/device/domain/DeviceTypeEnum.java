package com.wupol.myopia.business.core.device.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Classname DeviceTypeEnum
 * @Description 管理设备类型
 * @Date 2021/7/14 2:16 下午
 * @Author Jacob
 * @Version
 */
@AllArgsConstructor
@Getter
public enum DeviceTypeEnum {

    DEVICE_VS666("VS666",1 );

    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备类型
     */
    private int deviceType;
}

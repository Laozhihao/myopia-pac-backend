package com.wupol.myopia.business.core.device.constant;

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
public enum DeviceReportTemplateTypeEnum {

    DEVICE_REPORT_STANDARD("VS550报告-标准模板",1 ),
    DEVICE_REPORT_025D("VS550报告-0.25D分辨率",2 ),
    DEVICE_REPORT_001D("VS550报告-0.01D分辨率",3 );

    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备类型
     */
    private int deviceType;
}

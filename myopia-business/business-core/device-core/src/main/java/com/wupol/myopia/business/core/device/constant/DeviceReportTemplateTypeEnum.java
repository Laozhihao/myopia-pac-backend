package com.wupol.myopia.business.core.device.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Classname DeviceTypeEnum
 * @Description VS550报告模板
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
     * 报告名称
     */
    private String name;
    /**
     * 报告类型
     */
    private int type;
}

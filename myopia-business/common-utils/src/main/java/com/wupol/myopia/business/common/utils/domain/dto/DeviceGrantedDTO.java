package com.wupol.myopia.business.common.utils.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 已经授权设备
 *
 * @author Simple4H
 */
@Setter
@Getter
public class DeviceGrantedDTO implements Serializable {

    /**
     * 设备唯一id
     */
    private String deviceSn;

    /**
     * 类型 0-默认 1-vs666 2-灯箱 3-体脂秤 4-电脑验光 5-生物测量仪 6-眼底相机
     */
    private Integer type;

    /**
     * 蓝牙MAC地址
     */
    private String bluetoothMac;

    /**
     * 状态: 0-启用、1-停用
     */
    private Integer status;
}

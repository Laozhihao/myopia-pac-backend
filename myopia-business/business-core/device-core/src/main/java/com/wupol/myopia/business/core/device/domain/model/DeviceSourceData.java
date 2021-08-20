package com.wupol.myopia.business.core.device.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.wupol.myopia.business.core.device.domain.DeviceTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author jacob
 * @Date 2021-06-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_device_source_data")
public class DeviceSourceData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 设备类型(0=默认设备,1=vs666)
     */
    private Integer deviceType;

    /**
     * 患者id
     */
    private String patientId;

    /**
     * 设备id
     */
    private Integer deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备唯一id
     */
    private String deviceSn;

    /**
     * 原始数据
     */
    private String srcData;

    /**
     * 筛查机构id
     */
    private Integer screeningOrgId;

    /**
     * 筛查时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date screeningTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 构建一个新
     *
     * @param device
     * @param srcData
     * @param patientId
     * @param screeningTime
     * @return
     */
    public static DeviceSourceData getNewInstance(Device device, String srcData, String patientId, Date screeningTime) {
        DeviceSourceData deviceSourceData = new DeviceSourceData();
        deviceSourceData.deviceType = DeviceTypeEnum.DEVICE_VS666.getDeviceType();
        deviceSourceData.deviceCode = device.getDeviceCode();
        deviceSourceData.deviceId = device.getId();
        deviceSourceData.deviceSn = device.getDeviceSn();
        deviceSourceData.patientId = patientId;
        deviceSourceData.screeningOrgId = device.getBindingScreeningOrgId();
        deviceSourceData.srcData = srcData;
        deviceSourceData.screeningTime = screeningTime;
        return deviceSourceData;
    }
}

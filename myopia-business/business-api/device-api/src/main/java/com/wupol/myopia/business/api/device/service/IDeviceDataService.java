package com.wupol.myopia.business.api.device.service;

import com.wupol.myopia.business.aggregation.screening.domain.dto.DeviceDataRequestDTO;

/**
 * 设备数据接口
 *
 * @author Simple4H
 */
public interface IDeviceDataService {

    void uploadDate(DeviceDataRequestDTO requestDTO);
}

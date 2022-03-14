package com.wupol.myopia.business.api.device.service;

import com.wupol.myopia.business.aggregation.screening.domain.dto.DeviceDataRequestDTO;

/**
 * 设备数据接口
 *
 * @author Simple4H
 */
public interface IDeviceDataService {

    /**
     * 上传处理业务数据
     *
     * @param requestDTO 请求入参
     */
    void uploadDate(DeviceDataRequestDTO requestDTO);

    /**
     * 获取业务类型
     *
     * @return 业务类型 {@link com.wupol.myopia.business.api.device.domain.constant.BusinessTypeEnum}
     */
    Integer getBusinessType();
}

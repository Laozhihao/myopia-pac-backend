package com.wupol.myopia.business.core.device.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.device.domain.mapper.DeviceSourceDataMapper;
import com.wupol.myopia.business.core.device.domain.model.DeviceSourceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author jacob
 * @Date 2021-06-28
 */
@Service
public class DeviceSourceDataService extends BaseService<DeviceSourceDataMapper, DeviceSourceData> {
    @Autowired
    private DeviceScreeningDataService deviceScreeningDataService;

}

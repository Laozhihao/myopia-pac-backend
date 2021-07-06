package com.wupol.myopia.business.core.device.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.device.domain.mapper.DeviceMapper;
import com.wupol.myopia.business.core.device.domain.model.Device;
import org.springframework.stereotype.Service;

/**
 * @Author jacob
 * @Date 2021-06-28
 */
@Service
public class DeviceService extends BaseService<DeviceMapper, Device> {

    /**
     *  检查是否存在
     * @param deviceSn
     * @return
     */
    public Device getDeviceByDeviceSn(String deviceSn) {
        //todo 等合 治豪 的状态
        Device device = new Device().setDeviceSn(deviceSn).setStatus(1);
        return super.findOne(device);
    }
}

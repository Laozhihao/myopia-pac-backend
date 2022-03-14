package com.wupol.myopia.business.api.device.config;

import com.wupol.myopia.business.api.device.service.IDeviceDataService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工厂类
 *
 * @author Simple4H
 */
@Component
public class DeviceDataFactory implements ApplicationContextAware {

    private static Map<Integer, IDeviceDataService> deviceDataBeanMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, IDeviceDataService> map = applicationContext.getBeansOfType(IDeviceDataService.class);
        deviceDataBeanMap = new ConcurrentHashMap<>();
        map.forEach((key, value) -> deviceDataBeanMap.put(value.getBusinessType(), value));
    }

    public static IDeviceDataService getDeviceDataService(Integer type) {
        return deviceDataBeanMap.get(type);
    }
}

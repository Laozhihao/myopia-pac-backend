package com.wupol.myopia.business.aggregation.export.excel.config;

import com.wupol.myopia.business.aggregation.export.service.IScreeningDataService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 筛查数据工厂类
 *
 * @author Simple4H
 */
@Component
public class ScreeningDataFactory implements ApplicationContextAware {

    private static Map<Integer, IScreeningDataService> screeningDataServiceMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, IScreeningDataService> map = applicationContext.getBeansOfType(IScreeningDataService.class);
        screeningDataServiceMap = new ConcurrentHashMap<>();
        map.forEach((key, value) -> screeningDataServiceMap.put(value.getScreeningType(), value));
    }

    public static IScreeningDataService getScreeningDataService(Integer type) {
        return screeningDataServiceMap.get(type);
    }
}

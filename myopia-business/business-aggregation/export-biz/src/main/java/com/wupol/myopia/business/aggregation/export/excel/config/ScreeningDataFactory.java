package com.wupol.myopia.business.aggregation.export.excel.config;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.service.IScreeningDataService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 筛查数据工厂类
 *
 * @author Simple4H
 */
@Component
public class ScreeningDataFactory {

    private final List<IScreeningDataService> iScreeningDataServices;

    public ScreeningDataFactory(List<IScreeningDataService> iScreeningDataServices) {
        this.iScreeningDataServices = iScreeningDataServices;
    }

    public IScreeningDataService getScreeningDataService(Integer screeningType) {
        return iScreeningDataServices.stream().filter(service -> Objects.equals(service.getScreeningType(), screeningType)).findFirst().orElseThrow(() -> new BusinessException("获取类型异常"));
    }
}

package com.wupol.myopia.business.aggregation.screening.service.data.submit;

import com.wupol.myopia.base.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 数据上报工厂类
 *
 * @author Simple4H
 */
@Component
public class DataSubmitFactory {

    private final List<IDataSubmitService> dataSubmitService;

    public DataSubmitFactory(List<IDataSubmitService> dataSubmitService) {
        this.dataSubmitService = dataSubmitService;
    }

    public IDataSubmitService getDataSubmitService(Integer type) {
        return dataSubmitService.stream().filter(service -> Objects.equals(service.type(), type)).findFirst().orElseThrow(() -> new BusinessException("获取类型异常"));
    }
}

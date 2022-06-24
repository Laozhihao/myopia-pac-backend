package com.wupol.myopia.business.api.management.service.report;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CommonTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.MaxMinProportion;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * 比分比
 *
 * @author Simple4H
 */
@Service
public class HighLowProportionService {

    public <T extends CommonTable> HighLowProportion getHighLow(List<T> tables, Function<T, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new HighLowProportion();
        }
        if (tables.size() == 1) {
            T min = tables.get(0);
            return new HighLowProportion(min.getName(), String.valueOf(comparingFunction.apply(min)), min.getName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        T max = tables.get(tables.size() - 1);
        T min = tables.get(0);
        return new HighLowProportion(max.getName(), String.valueOf(comparingFunction.apply(max)), min.getName(), String.valueOf(comparingFunction.apply(min)));
    }

    /**
     * 年龄-戴眼
     */
    public <T extends CommonTable> MaxMinProportion getMaxMin(String proportion, List<T> tables, Function<T, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new MaxMinProportion();
        }
        if (tables.size() == 1) {
            T min = tables.get(0);
            return new MaxMinProportion(proportion, min.getName(), String.valueOf(comparingFunction.apply(min)), min.getName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        T max = tables.get(tables.size() - 1);
        T min = tables.get(0);
        return new MaxMinProportion(proportion, max.getName(), String.valueOf(comparingFunction.apply(max)), min.getName(), String.valueOf(comparingFunction.apply(min)));
    }

}

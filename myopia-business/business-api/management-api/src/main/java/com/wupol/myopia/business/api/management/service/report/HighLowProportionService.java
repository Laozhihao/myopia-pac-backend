package com.wupol.myopia.business.api.management.service.report;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CommonTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.MaxMinProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.RowSpan;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 比分比
 *
 * @author Simple4H
 */
@Service
public class HighLowProportionService {

    /**
     * 最高最低
     */
    public <T extends CommonTable> HighLowProportion getHighLow(List<T> tables, Function<T, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new HighLowProportion();
        }
        if (tables.size() == 1) {
            T min = tables.get(0);
            return new HighLowProportion(min.getName(), getStringValue(comparingFunction.apply(min)), min.getName(), getStringValue(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        T max = tables.get(tables.size() - 1);
        T min = tables.get(0);
        return new HighLowProportion(max.getName(), getStringValue(comparingFunction.apply(max)), min.getName(), getStringValue(comparingFunction.apply(min)));
    }

    /**
     * 最高最低描述
     */
    public <T extends CommonTable> MaxMinProportion getMaxMin(String proportion, List<T> tables, Function<T, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new MaxMinProportion();
        }
        if (tables.size() == 1) {
            T min = tables.get(0);
            return new MaxMinProportion(proportion, min.getName(), getStringValue(comparingFunction.apply(min)), min.getName(), getStringValue(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        T max = tables.get(tables.size() - 1);
        T min = tables.get(0);
        return new MaxMinProportion(proportion, max.getName(), getStringValue(comparingFunction.apply(max)), min.getName(), getStringValue(comparingFunction.apply(min)));
    }

    public <T extends RowSpan> MaxMinProportion getKindergartenMaxMin(String proportion, List<T> tables, Function<T, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new MaxMinProportion();
        }
        if (tables.size() == 1) {
            T min = tables.get(0);
            return new MaxMinProportion(proportion, min.getClassName(), getStringValue(comparingFunction.apply(min)), min.getClassName(), getStringValue(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        T max = tables.get(tables.size() - 1);
        T min = tables.get(0);
        return new MaxMinProportion(proportion, max.getClassName(), getStringValue(comparingFunction.apply(max)), min.getClassName(), getStringValue(comparingFunction.apply(min)));
    }

    public <T extends CommonTable> List<T> filterTable(List<T> tables) {
        if (tables.stream().anyMatch(s -> StringUtils.equals(s.getName(), "高中"))) {
            // 如果存在高中，则过滤普高和职高
            return tables.stream()
                    .filter(s -> !StringUtils.equals(s.getName(), SchoolAge.HIGH.desc))
                    .filter(s -> !StringUtils.equals(s.getName(), SchoolAge.VOCATIONAL_HIGH.desc))
                    .collect(Collectors.toList());
        }
        return tables;
    }

    private String getStringValue(Float f) {
        return String.format("%.2f", f);
    }

}

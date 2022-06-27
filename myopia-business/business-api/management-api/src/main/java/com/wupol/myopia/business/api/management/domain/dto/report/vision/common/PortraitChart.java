package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 纵向图表
 *
 * @author Simple4H
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortraitChart {

    /**
     * y轴
     */
    private List<String> y;

    /**
     * x轴
     */
    private List<ChartDetail> x;

    /**
     * 最大值
     */
    private String maxValue;

    public String getMaxValue() {
        if (CollectionUtils.isEmpty(x)) {
            return null;
        }
        return Collections.max(x.stream().flatMap(s -> s.getData().stream()).map(Float::valueOf).collect(Collectors.toList())).toString();
    }


}

package com.wupol.myopia.business.api.management.domain.dto.report.vision.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 堆积面积面图表
 *
 * @author Simple4H
 */
@Getter
@Setter
@NoArgsConstructor
public class StackedChart {

    private List<String> data;
    private String maxName;
    private String maxRatio;
    private String minName;
    private String minRatio;
    private String name;
    private String proportion;

    private String maxValue;

    public StackedChart(List<String> data, String maxName, String maxRatio, String minName, String minRatio, String name, String proportion) {
        this.data = data;
        this.maxName = maxName;
        this.maxRatio = maxRatio;
        this.minName = minName;
        this.minRatio = minRatio;
        this.name = name;
        this.proportion = proportion;
    }

    public String getMaxValue() {
        if (CollectionUtils.isEmpty(data)) {
            return null;
        }
        return Collections.max(data.stream().map(Float::valueOf).collect(Collectors.toList())).toString();
    }
}

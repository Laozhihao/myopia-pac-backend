package com.wupol.myopia.business.api.management.service.report;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.PrimaryScreeningInfoTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CommonTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.StackedChart;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 堆积面积面图表
 *
 * @author Simple4H
 */
@Service
public class StackedChartService {

    @Resource
    private CountAndProportionService countAndProportionService;

    public List<StackedChart> getOverallChart(List<PrimaryScreeningInfoTable> tables, List<StatConclusion> statConclusions, Long total) {

        return Lists.newArrayList(generateOverallChartDetail("视力低下", countAndProportionService.lowVision(statConclusions, total).getProportion(), tables, s -> Float.valueOf(s.getLowVisionProportion()), PrimaryScreeningInfoTable::getLowVisionProportion),
                generateOverallChartDetail("近视", countAndProportionService.myopia(statConclusions, total).getProportion(), tables, s -> Float.valueOf(s.getMyopiaProportion()), PrimaryScreeningInfoTable::getMyopiaProportion),
                generateOverallChartDetail("近视前期", countAndProportionService.earlyMyopia(statConclusions, total).getProportion(), tables, s -> Float.valueOf(s.getEarlyMyopiaProportion()), PrimaryScreeningInfoTable::getEarlyMyopiaProportion),
                generateOverallChartDetail("低度近视", countAndProportionService.lightMyopia(statConclusions, total).getProportion(), tables, s -> Float.valueOf(s.getLightMyopiaProportion()), PrimaryScreeningInfoTable::getLightMyopiaProportion),
                generateOverallChartDetail("高度近视", countAndProportionService.highMyopia(statConclusions, total).getProportion(), tables, s -> Float.valueOf(s.getHighMyopiaProportion()), PrimaryScreeningInfoTable::getHighMyopiaProportion),
                generateOverallChartDetail("建议就诊", countAndProportionService.getRecommendDoctor(statConclusions, total).getProportion(), tables, s -> Float.valueOf(s.getRecommendDoctorProportion()), PrimaryScreeningInfoTable::getRecommendDoctorProportion));
    }

    private  <T extends CommonTable> StackedChart generateOverallChartDetail(String name, String proportion, List<T> tables, Function<T, Float> comparingFunction, Function<T, String> getLowVisionProportionList) {
        if (CollectionUtils.isEmpty(tables)) {
            return new StackedChart();
        }
        tables.sort(Comparator.comparing(comparingFunction));
        T max = tables.get(tables.size() - 1);
        T min = tables.get(0);
        return new StackedChart(tables.stream().map(getLowVisionProportionList).collect(Collectors.toList()), max.getName(), getStringValue(comparingFunction.apply(max)), min.getName(), getStringValue(comparingFunction.apply(min)), name, proportion);
    }

    private String getStringValue(Float f) {
        return String.format("%.2f", f);
    }

    public List<String> a() {
        return Lists.newArrayList("视力低下", "近视", "近视前期", "低度近视", "高度近视", "建议就诊");
    }
}

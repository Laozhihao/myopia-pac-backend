package com.wupol.myopia.business.api.management.service.report;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary.WarningTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.ChartDetail;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.PortraitChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AgeWearingTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.CommonLowVisionTable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 纵向图表Service
 *
 * @author Simple4H
 */
@Service
public class PortraitChartService {

    @Resource
    private CommonChartService commonChartService;

    public PortraitChart lowVisionChart(List<CommonLowVisionTable> tables) {
        PortraitChart portraitChart = new PortraitChart();
        portraitChart.setY(Lists.newArrayList("轻度视力低下", "中度视力低下", "高度视力低下"));
        portraitChart.setX(tables.stream().map(table -> new ChartDetail(table.getName(), Lists.newArrayList(
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getLightLowVisionProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getMiddleLowVisionProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getHighLowVisionProportion()
        ))).collect(Collectors.toList()));
        return portraitChart;
    }

    public PortraitChart refractionChart(List<AstigmatismTable> tables) {
        PortraitChart portraitChart = new PortraitChart();
        portraitChart.setY(tables.stream().map(AstigmatismTable::getName).collect(Collectors.toList()));
        portraitChart.setX(commonChartService.astigmatismTableChartDetail(tables));
        return portraitChart;
    }

    public PortraitChart gradeRefractionChart(List<AstigmatismTable> tables) {
        PortraitChart portraitChart = new PortraitChart();
        portraitChart.setY(Lists.newArrayList("近视", "近视前期", "低度近视", "高度近视", "散光"));
        portraitChart.setX(tables.stream().map(table -> new ChartDetail(table.getName(), Lists.newArrayList(
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getMyopiaProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getEarlyMyopiaProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getLightMyopiaProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getHighMyopiaProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getAstigmatismProportion()
        ))).collect(Collectors.toList()));
        return portraitChart;
    }

    public PortraitChart wearingGlassesWearingChartY(List<AgeWearingTable> tables) {
        PortraitChart portraitChart = new PortraitChart();
        portraitChart.setY(Lists.newArrayList("不佩戴眼镜", "佩戴框架眼镜", "佩戴隐形眼镜", "佩戴塑形眼镜"));
        portraitChart.setX(tables.stream().map(table -> new ChartDetail(table.getName(), Lists.newArrayList(
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getNotWearingProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getGlassesProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getWearingContactProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getNightWearingProportion()
        ))).collect(Collectors.toList()));
        return portraitChart;
    }

    public PortraitChart visionCorrectionWearingChartY(List<AgeWearingTable> tables) {
        PortraitChart portraitChart = new PortraitChart();
        portraitChart.setY(Lists.newArrayList("足矫", "欠矫", "未矫"));
        portraitChart.setX(tables.stream().map(table -> new ChartDetail(table.getName(), Lists.newArrayList(
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getEnoughProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getUnderProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getUncorrectedProportion()
        ))).collect(Collectors.toList()));
        return portraitChart;
    }

    public PortraitChart warningChart(List<WarningTable> tables) {
        PortraitChart portraitChart = new PortraitChart();
        portraitChart.setY(tables.stream().map(WarningTable::getName).collect(Collectors.toList()));
        portraitChart.setX(Lists.newArrayList(
                new ChartDetail("0级预警", tables.stream().map(WarningTable::getZeroWarningProportion).collect(Collectors.toList())),
                new ChartDetail("1级预警", tables.stream().map(WarningTable::getOneWarningProportion).collect(Collectors.toList())),
                new ChartDetail("2级预警", tables.stream().map(WarningTable::getTwoWarningProportion).collect(Collectors.toList())),
                new ChartDetail("3级预警", tables.stream().map(WarningTable::getThreeWarningProportion).collect(Collectors.toList())),
                new ChartDetail("建议就诊", tables.stream().map(WarningTable::getRecommendDoctorProportion).collect(Collectors.toList()))
        ));
        return portraitChart;
    }

    public PortraitChart warningChart2(List<WarningTable> tables) {
        PortraitChart portraitChart = new PortraitChart();
        portraitChart.setY(Lists.newArrayList("0级预警", "1级预警", "2级预警", "3级预警", "建议就诊"));
        portraitChart.setX(tables.stream().map(table -> new ChartDetail(table.getName(), Lists.newArrayList(
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getZeroWarningProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getOneWarningProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getTwoWarningProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getThreeWarningProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getRecommendDoctorProportion()
        ))).collect(Collectors.toList()));
        return portraitChart;
    }

}

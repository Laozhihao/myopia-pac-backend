package com.wupol.myopia.business.api.management.service.report;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.AreaHistoryLowVisionTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.ChartDetail;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CommonTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HorizontalChart;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.MyopiaTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AgeWearingTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.CommonLowVisionTable;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.LowVisionLevelEnum;
import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 图表Service
 *
 * @author Simple4H
 */
@Service
public class HorizontalChartService {

    @Resource
    private CommonChartService commonChartService;

    @Resource
    private CountAndProportionService countAndProportionService;

    ArrayList<String> x = Lists.newArrayList("远视储备不足", "屈光不正", "屈光参差");

    public HorizontalChart areaLowVision(List<CommonLowVisionTable> tables, Boolean isAge) {
        HorizontalChart horizontalChart = new HorizontalChart();
        setHorizontalChartXName(tables, horizontalChart, isAge);
        horizontalChart.setY(Lists.newArrayList(new ChartDetail(LowVisionLevelEnum.LOW_VISION.desc, tables.stream().map(CommonLowVisionTable::getLowVisionProportion).collect(Collectors.toList()))));
        return horizontalChart;
    }

    public HorizontalChart lowVisionChart(List<CommonLowVisionTable> tables, Boolean isAge) {
        HorizontalChart horizontalChart = new HorizontalChart();
        tables = setHorizontalChartXName(tables, horizontalChart, isAge);
        horizontalChart.setY(Lists.newArrayList(
                new ChartDetail(LowVisionLevelEnum.LOW_VISION_LEVEL_LIGHT.desc, tables.stream().map(CommonLowVisionTable::getLightLowVisionProportion).collect(Collectors.toList())),
                new ChartDetail(LowVisionLevelEnum.LOW_VISION_LEVEL_MIDDLE.desc, tables.stream().map(CommonLowVisionTable::getMiddleLowVisionProportion).collect(Collectors.toList())),
                new ChartDetail(LowVisionLevelEnum.LOW_VISION_LEVEL_HIGH.desc, tables.stream().map(CommonLowVisionTable::getHighLowVisionProportion).collect(Collectors.toList()))
        ));
        return horizontalChart;
    }

    public HorizontalChart refractiveChart(List<RefractiveTable> tables) {
        HorizontalChart horizontalChart = new HorizontalChart();
        horizontalChart.setX(x);
        horizontalChart.setY(tables.stream().map(table -> new ChartDetail(table.getName(), Lists.newArrayList(
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getInsufficientProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getRefractiveErrorProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), table.getName())).collect(Collectors.toList()).get(0).getAnisometropiaProportion()
        ))).collect(Collectors.toList()));
        return horizontalChart;
    }

    public HorizontalChart genderRefractiveChart(List<RefractiveTable> tables) {
        HorizontalChart horizontalChart = new HorizontalChart();

        horizontalChart.setX(x);
        horizontalChart.setY(GenderEnum.genderList().stream().map(gender -> new ChartDetail(gender.cnDesc, Lists.newArrayList(
                tables.stream().filter(s -> StringUtils.equals(s.getName(), gender.cnDesc)).collect(Collectors.toList()).get(0).getInsufficientProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), gender.cnDesc)).collect(Collectors.toList()).get(0).getRefractiveErrorProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), gender.cnDesc)).collect(Collectors.toList()).get(0).getAnisometropiaProportion()
        ))).collect(Collectors.toList()));
        return horizontalChart;
    }

    public HorizontalChart kindergartenHistoryRefractive(List<RefractiveTable> tables) {
        HorizontalChart horizontalChart = new HorizontalChart();
        horizontalChart.setX(tables.stream().map(RefractiveTable::getName).collect(Collectors.toList()));
        horizontalChart.setY(Lists.newArrayList(
                new ChartDetail("远视储备不足", tables.stream().map(RefractiveTable::getInsufficientProportion).collect(Collectors.toList())),
                new ChartDetail("屈光不正", tables.stream().map(RefractiveTable::getRefractiveErrorProportion).collect(Collectors.toList())),
                new ChartDetail("屈光参差", tables.stream().map(RefractiveTable::getAnisometropiaProportion).collect(Collectors.toList()))
        ));
        return horizontalChart;
    }

    public HorizontalChart schoolAgeAstigmatismChart(List<AstigmatismTable> tables) {
        HorizontalChart horizontalChart = new HorizontalChart();
        horizontalChart.setX(tables.stream().map(AstigmatismTable::getName).collect(Collectors.toList()));
        horizontalChart.setY(commonChartService.astigmatismTableChartDetail(tables));
        return horizontalChart;
    }

    public HorizontalChart astigmatismMyopiaLevelChart(List<AstigmatismTable> tables) {
        HorizontalChart horizontalChart = new HorizontalChart();
        tables = setHorizontalChartXName(tables, horizontalChart, true);
        horizontalChart.setY(Lists.newArrayList(
                new ChartDetail("近视前期", tables.stream().map(AstigmatismTable::getEarlyMyopiaProportion).collect(Collectors.toList())),
                new ChartDetail("低度近视", tables.stream().map(AstigmatismTable::getLightMyopiaProportion).collect(Collectors.toList())),
                new ChartDetail("高度近视", tables.stream().map(AstigmatismTable::getHighMyopiaProportion).collect(Collectors.toList()))
        ));
        return horizontalChart;
    }

    public HorizontalChart astigmatismMyopiaChart(List<AstigmatismTable> tables) {
        HorizontalChart horizontalChart = new HorizontalChart();
        tables = setHorizontalChartXName(tables, horizontalChart, true);
        horizontalChart.setY(Lists.newArrayList(
                new ChartDetail("近视", tables.stream().map(AstigmatismTable::getMyopiaProportion).collect(Collectors.toList())),
                new ChartDetail("散光", tables.stream().map(AstigmatismTable::getAstigmatismProportion).collect(Collectors.toList()))));
        return horizontalChart;
    }

    public HorizontalChart genderAstigmatismChart(List<AstigmatismTable> tables) {
        HorizontalChart horizontalChart = new HorizontalChart();
        horizontalChart.setX(Lists.newArrayList("近视前期", "低度近视", "高度近视", "近视", "散光"));

        horizontalChart.setY(GenderEnum.genderList().stream().map(gender -> new ChartDetail(gender.cnDesc, Lists.newArrayList(
                tables.stream().filter(s -> StringUtils.equals(s.getName(), gender.cnDesc)).collect(Collectors.toList()).get(0).getEarlyMyopiaProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), gender.cnDesc)).collect(Collectors.toList()).get(0).getLightMyopiaProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), gender.cnDesc)).collect(Collectors.toList()).get(0).getHighMyopiaProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), gender.cnDesc)).collect(Collectors.toList()).get(0).getMyopiaProportion(),
                tables.stream().filter(s -> StringUtils.equals(s.getName(), gender.cnDesc)).collect(Collectors.toList()).get(0).getAstigmatismProportion())
        )).collect(Collectors.toList()));
        return horizontalChart;
    }

    public HorizontalChart primaryHistoryRefraction(List<AstigmatismTable> tables) {
        HorizontalChart horizontalChart = new HorizontalChart();
        horizontalChart.setX(tables.stream().map(AstigmatismTable::getName).collect(Collectors.toList()));
        horizontalChart.setY(Lists.newArrayList(
                new ChartDetail("近视", tables.stream().map(AstigmatismTable::getMyopiaProportion).collect(Collectors.toList())),
                new ChartDetail("近视前期", tables.stream().map(AstigmatismTable::getEarlyMyopiaProportion).collect(Collectors.toList())),
                new ChartDetail("低度近视", tables.stream().map(AstigmatismTable::getLightMyopiaProportion).collect(Collectors.toList())),
                new ChartDetail("高度近视", tables.stream().map(AstigmatismTable::getHighMyopiaProportion).collect(Collectors.toList()))
        ));
        return horizontalChart;
    }

    public HorizontalChart kLowVisionGenderChart(List<StatConclusion> statConclusions) {
        HorizontalChart horizontalChart = new HorizontalChart();
        horizontalChart.setX(statConclusions.stream().map(s -> GradeCodeEnum.getByCode(s.getSchoolGradeCode()).getName()).distinct().collect(Collectors.toList()));
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(s -> GradeCodeEnum.getByCode(s.getSchoolGradeCode()).getName()));

        ArrayList<String> mProportion = new ArrayList<>();
        ArrayList<String> fProportion = new ArrayList<>();

        long size = statConclusions.size();
        horizontalChart.getX().forEach(s -> {
            List<StatConclusion> v = collect.get(s);
            mProportion.add(BigDecimalUtil.divide(v.stream().filter(statConclusion -> Objects.equals(statConclusion.getGender(), GenderEnum.MALE.type)).count(), size));
            fProportion.add(BigDecimalUtil.divide(v.stream().filter(statConclusion -> Objects.equals(statConclusion.getGender(), GenderEnum.FEMALE.type)).count(), size));
        });
        horizontalChart.setY(Lists.newArrayList(
                new ChartDetail(GenderEnum.MALE.cnDesc, mProportion),
                new ChartDetail(GenderEnum.FEMALE.cnDesc, fProportion)
        ));
        return horizontalChart;
    }

    public HorizontalChart kGradeRefractive(List<StatConclusion> statConclusions) {
        HorizontalChart horizontalChart = new HorizontalChart();
        horizontalChart.setX(statConclusions.stream().map(s -> GradeCodeEnum.getByCode(s.getSchoolGradeCode()).getName()).distinct().collect(Collectors.toList()));
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(s -> GradeCodeEnum.getByCode(s.getSchoolGradeCode()).getName()));

        ArrayList<String> one = new ArrayList<>();
        ArrayList<String> two = new ArrayList<>();
        ArrayList<String> three = new ArrayList<>();
        long total = statConclusions.size();

        horizontalChart.getX().forEach(s -> {
            List<StatConclusion> v = collect.get(s);
            one.add(countAndProportionService.insufficient(v, total).getProportion());
            two.add(countAndProportionService.anisometropia(v, total).getProportion());
            three.add(countAndProportionService.refractiveError(v, total).getProportion());
        });
        horizontalChart.setY(Lists.newArrayList(
                new ChartDetail("远视储备不足", one),
                new ChartDetail("屈光参差", two),
                new ChartDetail("屈光不正", three)
        ));
        return horizontalChart;
    }

    public HorizontalChart kGradeWarning(List<StatConclusion> statConclusions) {
        HorizontalChart horizontalChart = new HorizontalChart();
        List<String> nameList = statConclusions.stream().map(s -> GradeCodeEnum.getByCode(s.getSchoolGradeCode()).getName()).distinct().collect(Collectors.toList());
        horizontalChart.setX(nameList);
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(s -> GradeCodeEnum.getByCode(s.getSchoolGradeCode()).getName()));

        ArrayList<String> zeroWarning = new ArrayList<>();
        ArrayList<String> oneWarning = new ArrayList<>();
        ArrayList<String> twoWarning = new ArrayList<>();
        ArrayList<String> threeWarning = new ArrayList<>();
        ArrayList<String> recommendDoctor = new ArrayList<>();
        long total = statConclusions.size();

        horizontalChart.getX().forEach(s -> {
            List<StatConclusion> v = collect.get(s);
            zeroWarning.add(countAndProportionService.zeroAndSPWarning(v, total).getProportion());
            oneWarning.add(countAndProportionService.oneWarning(v, total).getProportion());
            twoWarning.add(countAndProportionService.twoWarning(v, total).getProportion());
            threeWarning.add(countAndProportionService.threeWarning(v, total).getProportion());
            recommendDoctor.add(countAndProportionService.getRecommendDoctor(v, total).getProportion());
        });
        horizontalChart.setY(Lists.newArrayList(
                new ChartDetail(WarningLevel.ZERO.desc, zeroWarning),
                new ChartDetail(WarningLevel.ONE.desc, oneWarning),
                new ChartDetail(WarningLevel.TWO.desc, twoWarning),
                new ChartDetail(WarningLevel.THREE.desc, threeWarning),
                new ChartDetail("建议就诊", recommendDoctor)
        ));
        return horizontalChart;
    }

    public HorizontalChart myopiaTableChart(List<MyopiaTable> tables) {
        HorizontalChart horizontalChart = new HorizontalChart();
        horizontalChart.setX(tables.stream().map(MyopiaTable::getName).collect(Collectors.toList()));
        horizontalChart.setY(Lists.newArrayList(
                new ChartDetail("视力低下", tables.stream().map(MyopiaTable::getLowVisionProportion).collect(Collectors.toList())),
                new ChartDetail("近视", tables.stream().map(MyopiaTable::getMyopiaProportion).collect(Collectors.toList()))
        ));
        return horizontalChart;
    }

    public HorizontalChart myopiaLevelTableChart(List<MyopiaTable> tables) {
        HorizontalChart horizontalChart = new HorizontalChart();
        horizontalChart.setX(tables.stream().map(MyopiaTable::getName).collect(Collectors.toList()));
        horizontalChart.setY(Lists.newArrayList(
                new ChartDetail("近视前期", tables.stream().map(MyopiaTable::getEarlyProportion).collect(Collectors.toList())),
                new ChartDetail("低度近视", tables.stream().map(MyopiaTable::getLightProportion).collect(Collectors.toList())),
                new ChartDetail("高度近视", tables.stream().map(MyopiaTable::getHighProportion).collect(Collectors.toList()))
        ));
        return horizontalChart;
    }

    public HorizontalChart primaryWearingGlassesChart(List<AgeWearingTable> tables, Boolean isAge) {
        HorizontalChart horizontalChart = new HorizontalChart();
        tables = setHorizontalChartXName(tables, horizontalChart, isAge);
        horizontalChart.setY(Lists.newArrayList(
                new ChartDetail("不佩戴眼镜", tables.stream().map(AgeWearingTable::getNotWearingProportion).collect(Collectors.toList())),
                new ChartDetail("佩戴框架眼镜", tables.stream().map(AgeWearingTable::getGlassesProportion).collect(Collectors.toList())),
                new ChartDetail("佩戴隐形眼镜", tables.stream().map(AgeWearingTable::getWearingContactProportion).collect(Collectors.toList())),
                new ChartDetail("夜戴角膜塑形镜", tables.stream().map(AgeWearingTable::getNightWearingProportion).collect(Collectors.toList()))
        ));
        return horizontalChart;
    }

    public HorizontalChart primaryVisionCorrectionChart(List<AgeWearingTable> tables, Boolean isAge) {
        HorizontalChart horizontalChart = new HorizontalChart();
        tables = setHorizontalChartXName(tables, horizontalChart, isAge);
        horizontalChart.setY(Lists.newArrayList(
                new ChartDetail("足矫", tables.stream().map(AgeWearingTable::getEnoughProportion).collect(Collectors.toList())),
                new ChartDetail("欠矫", tables.stream().map(AgeWearingTable::getUnderProportion).collect(Collectors.toList())),
                new ChartDetail("未矫", tables.stream().map(AgeWearingTable::getUncorrectedProportion).collect(Collectors.toList()))
        ));
        return horizontalChart;
    }

    public HorizontalChart areaHistoryLowVisionChart(List<AreaHistoryLowVisionTable> tables) {
        HorizontalChart horizontalChart = new HorizontalChart();
        horizontalChart.setX(tables.stream().map(AreaHistoryLowVisionTable::getName).collect(Collectors.toList()));
        horizontalChart.setY(Lists.newArrayList(
                new ChartDetail("视力低常", tables.stream().map(AreaHistoryLowVisionTable::getKLowVisionProportion).collect(Collectors.toList())),
                new ChartDetail("视力低下", tables.stream().map(AreaHistoryLowVisionTable::getLowVisionProportion).collect(Collectors.toList()))
        ));
        return horizontalChart;
    }

    private <T extends CommonTable> List<T> setHorizontalChartXName(List<T> t, HorizontalChart horizontalChart, Boolean isAge) {
        if (isAge) {
            t = t.stream().filter(s -> s.getValidCount() != 0L).collect(Collectors.toList());
            horizontalChart.setX(t.stream()
                    .map(s -> StringUtils.replace(s.getName(), "年龄", "*"))
                    .collect(Collectors.toList()));
        } else {
            horizontalChart.setX(t.stream().map(T::getName).collect(Collectors.toList()));
        }
        return t;
    }
}
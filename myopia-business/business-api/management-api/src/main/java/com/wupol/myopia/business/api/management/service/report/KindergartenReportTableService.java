package com.wupol.myopia.business.api.management.service.report;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.*;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 幼儿园表格
 *
 * @author Simple4H
 */
@Service
public class KindergartenReportTableService {

    @Resource
    private CountAndProportionService countAndProportionService;

    @Resource
    private CommonReportService commonReportService;

    public List<GradeRefractive.Table> gradeRefractiveTables(List<StatConclusion> statConclusions, Long total) {
        List<GradeRefractive.Table> tables = new ArrayList<>();
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        List<String> kindergartenCodeList = GradeCodeEnum.kindergartenSchoolCode();
        kindergartenCodeList.forEach(code -> {
            List<StatConclusion> list = collect.getOrDefault(code, null);
            if (Objects.isNull(list)) {
                return;
            }
            AtomicBoolean isFirst = new AtomicBoolean(true);
            Map<String, List<StatConclusion>> classList = list.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolClassName));
            classList.forEach((k, v) -> {
                GradeRefractive.Table table = new GradeRefractive.Table();
                table.setName(GradeCodeEnum.getDesc(code));
                table.setClassName(k);
                isFirst.set(setRowSpan(table, isFirst, (long) classList.size()));
                extracted(tables, v, table, total);
            });
        });
        GradeRefractive.Table table = new GradeRefractive.Table();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setClassName(CommonReportService.TOTAL_NAME);
        extracted(tables, statConclusions, table, total);
        return tables;
    }

    private void extracted(List<GradeRefractive.Table> tables, List<StatConclusion> v, GradeRefractive.Table table, Long total) {
        table.setValidCount(v.size());
        CountAndProportion insufficient = countAndProportionService.insufficient(v, total);
        table.setInsufficientStudentCount(insufficient.getCount());
        table.setInsufficientProportion(insufficient.getProportion());
        CountAndProportion refractiveError = countAndProportionService.refractiveError(v, total);
        table.setRefractiveErrorStudentCount(refractiveError.getCount());
        table.setRefractiveErrorProportion(refractiveError.getProportion());
        CountAndProportion anisometropia = countAndProportionService.anisometropia(v, total);
        table.setAnisometropiaStudentCount(anisometropia.getCount());
        table.setAnisometropiaProportion(anisometropia.getProportion());
        tables.add(table);
    }

    public List<GradeWarning.Table> kindergartenGradeWarningTable(List<StatConclusion> statConclusions, Long total) {
        List<GradeWarning.Table> tables = new ArrayList<>();
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        List<String> kindergartenCodeList = GradeCodeEnum.kindergartenSchoolCode();
        kindergartenCodeList.forEach(code -> {
            List<StatConclusion> list = collect.getOrDefault(code, null);
            if (Objects.isNull(list)) {
                return;
            }
            AtomicBoolean isFirst = new AtomicBoolean(true);
            Map<String, List<StatConclusion>> classList = list.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolClassName));
            classList.forEach((k, v) -> {
                GradeWarning.Table table = new GradeWarning.Table();
                table.setName(GradeCodeEnum.getDesc(code));
                table.setClassName(k);
                isFirst.set(setRowSpan(table, isFirst, (long) classList.size()));
                extracted(tables, v, table, total);
            });
        });
        GradeWarning.Table table = new GradeWarning.Table();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setClassName(CommonReportService.TOTAL_NAME);
        extracted(tables, statConclusions, table, total);
        return tables;
    }

    private void extracted(List<GradeWarning.Table> tables, List<StatConclusion> v, GradeWarning.Table table, Long total) {
        table.setValidCount(v.size());
        CountAndProportion zeroWarning = countAndProportionService.zeroWarning(v, total);
        table.setZeroWarningCount(zeroWarning.getCount());
        table.setZeroWarningProportion(zeroWarning.getProportion());
        CountAndProportion oneWarning = countAndProportionService.oneWarning(v, total);
        table.setOneWarningCount(oneWarning.getCount());
        table.setOneWarningProportion(oneWarning.getProportion());
        CountAndProportion twoWarning = countAndProportionService.twoWarning(v, total);
        table.setTwoWarningCount(twoWarning.getCount());
        table.setTwoWarningProportion(twoWarning.getProportion());
        CountAndProportion threeWarning = countAndProportionService.threeWarning(v, total);
        table.setThreeWarningCount(threeWarning.getCount());
        table.setThreeWarningProportion(threeWarning.getProportion());
        CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(v, total);
        table.setRecommendDoctorCount(recommendDoctor.getCount());
        table.setRecommendDoctorProportion(recommendDoctor.getProportion());
        CountAndProportion warning = countAndProportionService.warning(v, total);
        table.setWarningCount(warning.getCount());
        table.setWarningProportion(warning.getProportion());
        tables.add(table);
    }

    public List<GradeLowVision.Table> gradeLowVisionTable(List<StatConclusion> statConclusions, Long total) {
        List<GradeLowVision.Table> tables = new ArrayList<>();
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        List<String> kindergartenCodeList = GradeCodeEnum.kindergartenSchoolCode();
        kindergartenCodeList.forEach(code -> {
            List<StatConclusion> list = collect.getOrDefault(code, null);
            if (Objects.isNull(list)) {
                return;
            }
            AtomicBoolean isFirst = new AtomicBoolean(true);
            Map<String, List<StatConclusion>> classList = list.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolClassName));
            classList.forEach((k, v) -> {
                GradeLowVision.Table table = new GradeLowVision.Table();
                table.setName(GradeCodeEnum.getDesc(code));
                table.setClassName(k);
                isFirst.set(setRowSpan(table, isFirst, (long) classList.size()));
                extracted(v, tables, table, total);
            });
        });
        GradeLowVision.Table table = new GradeLowVision.Table();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setClassName(CommonReportService.TOTAL_NAME);
        extracted(statConclusions, tables, table, total);
        return tables;
    }

    private void extracted(List<StatConclusion> statConclusions, List<GradeLowVision.Table> tables, GradeLowVision.Table table, Long total) {
        table.setValidCount(statConclusions.size());
        CountAndProportion male = countAndProportionService.male(statConclusions);
        table.setMCount(male.getCount());
        table.setMProportion(male.getProportion());
        CountAndProportion female = countAndProportionService.female(statConclusions);
        table.setFCount(female.getCount());
        table.setFProportion(female.getProportion());
        table.setLowVisionProportion(countAndProportionService.lowVision(statConclusions, total).getProportion());
        tables.add(table);
    }

    public List<SexLowVision.Table> sexLowVisionTable(List<StatConclusion> statConclusions, Long total) {
        List<SexLowVision.Table> tables = new ArrayList<>();
        List<Integer> genderList = GenderEnum.genderTypeList();
        Map<Integer, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        genderList.forEach(gender -> {
            SexLowVision.Table table = new SexLowVision.Table();
            table.setName(GenderEnum.getCnName(gender));
            List<StatConclusion> v = collect.getOrDefault(gender, new ArrayList<>());
            extracted(v, tables, table, total);
        });
        SexLowVision.Table table = new SexLowVision.Table();
        table.setName(CommonReportService.TOTAL_NAME);
        extracted(statConclusions, tables, table, total);
        return tables;
    }

    private void extracted(List<StatConclusion> statConclusions, List<SexLowVision.Table> tables, SexLowVision.Table table, Long total) {
        table.setValidCount(commonReportService.getValidList(statConclusions).size());
        CountAndProportion lowVision = countAndProportionService.lowVision(statConclusions, total);
        table.setLowVisionCount(lowVision.getCount());
        table.setProportion(lowVision.getProportion());
        table.setAvgVision(commonReportService.getAvgVision(statConclusions));
        tables.add(table);
    }

    private <T extends RowSpan> Boolean setRowSpan(T t, AtomicBoolean isFirst, Long size) {
        if (isFirst.get()) {
            isFirst.set(false);
            t.setRowSpan(size);
        } else {
            t.setRowSpan(0L);
        }
        return isFirst.get();
    }
}

package com.wupol.myopia.business.api.management.service.report;

import com.wupol.myopia.base.util.AgeGeneration;
import com.wupol.myopia.base.util.AgeGenerationEnum;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.base.util.ChainRatio;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.KindergartenScreeningInfoTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.PrimaryScreeningInfoTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary.WarningTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.GenderSexLowVisionTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.SchoolAgeGenderTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.SchoolAgeLowVisionTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.SchoolHistoryLowVisionTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.MyopiaTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.GradeLowVision;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.GradeRefractive;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.GradeWarning;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.SexLowVision;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AgeWearingTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.LowVisionTable;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报告表格
 *
 * @author Simple4H
 */
@Service
public class ScreeningReportTableService {

    @Resource
    private CommonReportService commonReportService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private CountAndProportionService countAndProportionService;

    /**
     * 不同学龄段不同程度视力情况
     */
    public List<SchoolAgeLowVisionTable> gradeSchoolAgeLowVisionTable(List<StatConclusion> statConclusions) {
        List<SchoolAgeLowVisionTable> tables = new ArrayList<>();

        Map<Integer, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));
        // 学龄段
        collect.forEach((k, v) -> {
            Map<String, List<StatConclusion>> gradeMap = v.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
            // 年级
            for (Map.Entry<String, List<StatConclusion>> entry : gradeMap.entrySet()) {
                SchoolAgeLowVisionTable table = new SchoolAgeLowVisionTable();

                List<StatConclusion> value = entry.getValue();
                table.setName(GradeCodeEnum.getName(entry.getKey()));
                table.setValidCount(value.size());
                table.setAvgVision(commonReportService.getAvgVision(value));

                CountAndProportion lowVision = countAndProportionService.lowVision(value);
                table.setLowVisionCount(lowVision.getCount());
                table.setLowVisionProportion(lowVision.getProportion());

                CountAndProportion lightLowVision = countAndProportionService.lightLowVision(value);
                table.setLightLowVisionCount(lightLowVision.getCount());
                table.setLightLowVisionProportion(lightLowVision.getProportion());

                CountAndProportion middleLowVision = countAndProportionService.middleLowVision(value);
                table.setMiddleLowVisionCount(middleLowVision.getCount());
                table.setMiddleLowVisionProportion(middleLowVision.getProportion());

                CountAndProportion highLowVision = countAndProportionService.highLowVision(value);
                table.setHighLowVisionCount(highLowVision.getCount());
                table.setHighLowVisionProportion(highLowVision.getProportion());
                tables.add(table);
            }
            SchoolAgeLowVisionTable schoolAgeTotal = new SchoolAgeLowVisionTable();

            schoolAgeTotal.setName(SchoolAge.get(k).desc);
            schoolAgeTotal.setValidCount(v.size());
            schoolAgeTotal.setAvgVision(commonReportService.getAvgVision(v));

            CountAndProportion kLowVision = countAndProportionService.lowVision(v);
            schoolAgeTotal.setLowVisionCount(kLowVision.getCount());
            schoolAgeTotal.setLowVisionProportion(kLowVision.getProportion());

            CountAndProportion lightLowVision = countAndProportionService.lightLowVision(v);
            schoolAgeTotal.setLightLowVisionCount(lightLowVision.getCount());
            schoolAgeTotal.setLightLowVisionProportion(lightLowVision.getProportion());

            CountAndProportion middleLowVision = countAndProportionService.middleLowVision(v);
            schoolAgeTotal.setMiddleLowVisionCount(middleLowVision.getCount());
            schoolAgeTotal.setMiddleLowVisionProportion(middleLowVision.getProportion());

            CountAndProportion highLowVision = countAndProportionService.highLowVision(v);
            schoolAgeTotal.setHighLowVisionCount(highLowVision.getCount());
            schoolAgeTotal.setHighLowVisionProportion(highLowVision.getProportion());
            tables.add(schoolAgeTotal);
        });

        SchoolAgeLowVisionTable total = new SchoolAgeLowVisionTable();

        total.setName(CommonReportService.TOTAL_NAME);
        total.setValidCount(statConclusions.size());
        total.setAvgVision(commonReportService.getAvgVision(statConclusions));

        CountAndProportion lowVision = countAndProportionService.lowVision(statConclusions);
        total.setLowVisionCount(lowVision.getCount());
        total.setLowVisionProportion(lowVision.getProportion());

        CountAndProportion lightLowVision = countAndProportionService.lightLowVision(statConclusions);
        total.setLightLowVisionCount(lightLowVision.getCount());
        total.setLightLowVisionProportion(lightLowVision.getProportion());

        CountAndProportion middleLowVision = countAndProportionService.middleLowVision(statConclusions);
        total.setMiddleLowVisionCount(middleLowVision.getCount());
        total.setMiddleLowVisionProportion(middleLowVision.getProportion());

        CountAndProportion highLowVision = countAndProportionService.highLowVision(statConclusions);
        total.setHighLowVisionCount(highLowVision.getCount());
        total.setHighLowVisionProportion(highLowVision.getProportion());
        tables.add(total);

        return tables;
    }

    public List<SchoolAgeLowVisionTable> schoolAgeLowVisionTables(List<StatConclusion> statConclusions) {
        List<SchoolAgeLowVisionTable> tables = new ArrayList<>();
        TwoTuple<Integer, Integer> statAgeRange = commonReportService.getStatAgeRange(statConclusions);
        List<AgeGeneration> ageList = AgeGenerationEnum.getAllList(statAgeRange.getFirst(), statAgeRange.getSecond());

        for (AgeGeneration age : ageList) {
            SchoolAgeLowVisionTable table = new SchoolAgeLowVisionTable();
            table.setName(age.getDesc());
            List<StatConclusion> value = statConclusions.stream()
                    .filter(s -> s.getAge() >= age.getLeftAge() && s.getAge() < age.getRightAge()).collect(Collectors.toList());

            table.setValidCount(value.size());
            table.setAvgVision(commonReportService.getAvgVision(value));

            CountAndProportion lowVision = countAndProportionService.lowVision(value);
            table.setLowVisionCount(lowVision.getCount());
            table.setLowVisionProportion(lowVision.getProportion());

            CountAndProportion lightLowVision = countAndProportionService.lightLowVision(value);
            table.setLightLowVisionCount(lightLowVision.getCount());
            table.setLightLowVisionProportion(lightLowVision.getProportion());

            CountAndProportion middleLowVision = countAndProportionService.middleLowVision(value);
            table.setMiddleLowVisionCount(middleLowVision.getCount());
            table.setMiddleLowVisionProportion(middleLowVision.getProportion());

            CountAndProportion highLowVision = countAndProportionService.highLowVision(value);
            table.setHighLowVisionCount(highLowVision.getCount());
            table.setHighLowVisionProportion(highLowVision.getProportion());
            tables.add(table);
        }

        SchoolAgeLowVisionTable total = new SchoolAgeLowVisionTable();
        total.setName(CommonReportService.TOTAL_NAME);

        total.setValidCount(statConclusions.size());
        total.setAvgVision(commonReportService.getAvgVision(statConclusions));

        CountAndProportion lowVision = countAndProportionService.lowVision(statConclusions);
        total.setLowVisionCount(lowVision.getCount());
        total.setLowVisionProportion(lowVision.getProportion());

        CountAndProportion lightLowVision = countAndProportionService.lightLowVision(statConclusions);
        total.setLightLowVisionCount(lightLowVision.getCount());
        total.setLightLowVisionProportion(lightLowVision.getProportion());

        CountAndProportion middleLowVision = countAndProportionService.middleLowVision(statConclusions);
        total.setMiddleLowVisionCount(middleLowVision.getCount());
        total.setMiddleLowVisionProportion(middleLowVision.getProportion());

        CountAndProportion highLowVision = countAndProportionService.highLowVision(statConclusions);
        total.setHighLowVisionCount(highLowVision.getCount());
        total.setHighLowVisionProportion(highLowVision.getProportion());
        tables.add(total);
        return tables;
    }

    public List<RefractiveTable> genderRefractiveTable(List<StatConclusion> statConclusions) {
        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<RefractiveTable> tables = new ArrayList<>();
        List<Integer> genderList = GenderEnum.genderList();
        genderList.forEach(gender -> {
            RefractiveTable table = new RefractiveTable();
            table.setName(GenderEnum.getName(gender));
            List<StatConclusion> v = genderMap.getOrDefault(gender, new ArrayList<>());
            table.setValidCount(v.size());

            CountAndProportion insufficient = countAndProportionService.insufficient(v);
            table.setInsufficientStudentCount(insufficient.getCount());
            table.setInsufficientProportion(insufficient.getProportion());

            CountAndProportion refractiveError = countAndProportionService.refractiveError(v);
            table.setRefractiveErrorStudentCount(refractiveError.getCount());
            table.setRefractiveErrorProportion(refractiveError.getProportion());

            CountAndProportion anisometropia = countAndProportionService.anisometropia(v);
            table.setAnisometropiaStudentCount(anisometropia.getCount());
            table.setAnisometropiaProportion(anisometropia.getProportion());

            CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(v);
            table.setRecommendDoctorCount(recommendDoctor.getCount());
            table.setRecommendDoctorProportion(recommendDoctor.getProportion());
            tables.add(table);
        });

        RefractiveTable total = new RefractiveTable();
        total.setName(CommonReportService.TOTAL_NAME);
        total.setValidCount(statConclusions.size());

        CountAndProportion insufficient = countAndProportionService.insufficient(statConclusions);
        total.setInsufficientStudentCount(insufficient.getCount());
        total.setInsufficientProportion(insufficient.getProportion());

        CountAndProportion refractiveError = countAndProportionService.refractiveError(statConclusions);
        total.setRefractiveErrorStudentCount(refractiveError.getCount());
        total.setRefractiveErrorProportion(refractiveError.getProportion());

        CountAndProportion anisometropia = countAndProportionService.anisometropia(statConclusions);
        total.setAnisometropiaStudentCount(anisometropia.getCount());
        total.setAnisometropiaProportion(anisometropia.getProportion());

        CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(statConclusions);
        total.setRecommendDoctorCount(recommendDoctor.getCount());
        total.setRecommendDoctorProportion(recommendDoctor.getProportion());
        tables.add(total);
        return tables;
    }

    /**
     * 不同学龄段屈光筛查情况
     */
    public List<RefractiveTable> schoolAgeRefractiveTable(List<StatConclusion> statConclusions) {
        Map<String, List<StatConclusion>> gradeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        List<RefractiveTable> tables = new ArrayList<>();
        gradeMap.forEach((k, v) -> {
            RefractiveTable table = new RefractiveTable();
            table.setName(GradeCodeEnum.getName(k));
            table.setValidCount(v.size());

            CountAndProportion insufficient = countAndProportionService.insufficient(v);
            table.setInsufficientStudentCount(insufficient.getCount());
            table.setInsufficientProportion(insufficient.getProportion());

            CountAndProportion refractiveError = countAndProportionService.refractiveError(v);
            table.setRefractiveErrorStudentCount(refractiveError.getCount());
            table.setRefractiveErrorProportion(refractiveError.getProportion());

            CountAndProportion anisometropia = countAndProportionService.anisometropia(v);
            table.setAnisometropiaStudentCount(anisometropia.getCount());
            table.setAnisometropiaProportion(anisometropia.getProportion());

            CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(v);
            table.setRecommendDoctorCount(recommendDoctor.getCount());
            table.setRecommendDoctorProportion(recommendDoctor.getProportion());
            tables.add(table);
        });

        RefractiveTable table = new RefractiveTable();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());

        CountAndProportion insufficient = countAndProportionService.insufficient(statConclusions);
        table.setInsufficientStudentCount(insufficient.getCount());
        table.setInsufficientProportion(insufficient.getProportion());

        CountAndProportion refractiveError = countAndProportionService.refractiveError(statConclusions);
        table.setRefractiveErrorStudentCount(refractiveError.getCount());
        table.setRefractiveErrorProportion(refractiveError.getProportion());

        CountAndProportion anisometropia = countAndProportionService.anisometropia(statConclusions);
        table.setAnisometropiaStudentCount(anisometropia.getCount());
        table.setAnisometropiaProportion(anisometropia.getProportion());

        CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(statConclusions);
        table.setRecommendDoctorCount(recommendDoctor.getCount());
        table.setRecommendDoctorProportion(recommendDoctor.getProportion());
        tables.add(table);

        return tables;
    }

    public List<RefractiveTable> ageRefractiveTable(List<StatConclusion> statConclusions) {
        List<RefractiveTable> tables = new ArrayList<>();
        TwoTuple<Integer, Integer> statAgeRange = commonReportService.getStatAgeRange(statConclusions);
        List<AgeGeneration> ageList = AgeGenerationEnum.getKList(statAgeRange.getFirst(), statAgeRange.getSecond());

        ageList.forEach(age -> {
            RefractiveTable table = new RefractiveTable();
            List<StatConclusion> list = statConclusions.stream()
                    .filter(s -> s.getAge() >= age.getLeftAge() && s.getAge() < age.getRightAge()).collect(Collectors.toList());

            table.setName(age.getDesc());
            table.setValidCount(list.size());

            CountAndProportion insufficient = countAndProportionService.insufficient(list);
            table.setInsufficientStudentCount(insufficient.getCount());
            table.setInsufficientProportion(insufficient.getProportion());

            CountAndProportion refractiveError = countAndProportionService.refractiveError(list);
            table.setRefractiveErrorStudentCount(refractiveError.getCount());
            table.setRefractiveErrorProportion(refractiveError.getProportion());

            CountAndProportion anisometropia = countAndProportionService.anisometropia(list);
            table.setAnisometropiaStudentCount(anisometropia.getCount());
            table.setAnisometropiaProportion(anisometropia.getProportion());

            CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(list);
            table.setRecommendDoctorCount(recommendDoctor.getCount());
            table.setRecommendDoctorProportion(recommendDoctor.getProportion());
            tables.add(table);
        });

        RefractiveTable table = new RefractiveTable();

        table.setName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());

        CountAndProportion insufficient = countAndProportionService.insufficient(statConclusions);
        table.setInsufficientStudentCount(insufficient.getCount());
        table.setInsufficientProportion(insufficient.getProportion());

        CountAndProportion refractiveError = countAndProportionService.refractiveError(statConclusions);
        table.setRefractiveErrorStudentCount(refractiveError.getCount());
        table.setRefractiveErrorProportion(refractiveError.getProportion());

        CountAndProportion anisometropia = countAndProportionService.anisometropia(statConclusions);
        table.setAnisometropiaStudentCount(anisometropia.getCount());
        table.setAnisometropiaProportion(anisometropia.getProportion());

        CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(statConclusions);
        table.setRecommendDoctorCount(recommendDoctor.getCount());
        table.setRecommendDoctorProportion(recommendDoctor.getProportion());
        tables.add(table);
        return tables;
    }

    public List<RefractiveTable> kHistoryRefractiveTable(List<StatConclusion> statConclusions) {
        List<RefractiveTable> tables = new ArrayList<>();
        List<ChainRatio> historyDate = commonReportService.getHistoryDate(statConclusions);
        historyDate.forEach(s -> {
            RefractiveTable table = new RefractiveTable();
            table.setName(s.getName());

            List<StatConclusion> list = statConclusions.stream()
                    .filter(x -> x.getCreateTime().after(s.getStartDate()))
                    .filter(y -> y.getCreateTime().before(s.getEndDate()))
                    .collect(Collectors.toList());

            table.setName(s.getName());
            table.setValidCount(list.size());

            CountAndProportion insufficient = countAndProportionService.insufficient(list);
            table.setInsufficientStudentCount(insufficient.getCount());
            table.setInsufficientProportion(insufficient.getProportion());

            CountAndProportion refractiveError = countAndProportionService.refractiveError(list);
            table.setRefractiveErrorStudentCount(refractiveError.getCount());
            table.setRefractiveErrorProportion(refractiveError.getProportion());

            CountAndProportion anisometropia = countAndProportionService.anisometropia(list);
            table.setAnisometropiaStudentCount(anisometropia.getCount());
            table.setAnisometropiaProportion(anisometropia.getProportion());

            CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(list);
            table.setRecommendDoctorCount(recommendDoctor.getCount());
            table.setRecommendDoctorProportion(recommendDoctor.getProportion());

            CountAndProportion lowVision = countAndProportionService.lowVision(list);
            table.setLowVisionStudentCount(lowVision.getCount());
            table.setLowVisionProportion(lowVision.getProportion());
            tables.add(table);

        });
        return tables;
    }

    public List<AstigmatismTable> genderPrimaryRefractiveTable(List<StatConclusion> statConclusions) {
        List<AstigmatismTable> tables = new ArrayList<>();
        List<Integer> genderList = GenderEnum.genderList();
        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        genderList.forEach(gender -> {
            List<StatConclusion> v = genderMap.getOrDefault(gender, new ArrayList<>());
            AstigmatismTable table = new AstigmatismTable();
            table.setName(GenderEnum.getName(gender));
            table.setValidCount(v.size());

            CountAndProportion myopia = countAndProportionService.myopia(v);
            table.setMyopiaCount(myopia.getCount());
            table.setMyopiaProportion(myopia.getProportion());

            CountAndProportion astigmatism = countAndProportionService.astigmatism(v);
            table.setAstigmatismCount(astigmatism.getCount());
            table.setAstigmatismProportion(astigmatism.getProportion());

            CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(v);
            table.setEarlyMyopiaCount(earlyMyopia.getCount());
            table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

            CountAndProportion lightMyopia = countAndProportionService.lightMyopia(v);
            table.setLightMyopiaCount(lightMyopia.getCount());
            table.setLightMyopiaProportion(lightMyopia.getProportion());

            CountAndProportion highMyopia = countAndProportionService.highMyopia(v);
            table.setHighMyopiaCount(highMyopia.getCount());
            table.setHighMyopiaProportion(highMyopia.getProportion());
            tables.add(table);
        });
        AstigmatismTable table = new AstigmatismTable();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());

        CountAndProportion myopia = countAndProportionService.myopia(statConclusions);
        table.setMyopiaCount(myopia.getCount());
        table.setMyopiaProportion(myopia.getProportion());

        CountAndProportion astigmatism = countAndProportionService.astigmatism(statConclusions);
        table.setAstigmatismCount(astigmatism.getCount());
        table.setAstigmatismProportion(astigmatism.getProportion());

        CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(statConclusions);
        table.setEarlyMyopiaCount(earlyMyopia.getCount());
        table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

        CountAndProportion lightMyopia = countAndProportionService.lightMyopia(statConclusions);
        table.setLightMyopiaCount(lightMyopia.getCount());
        table.setLightMyopiaProportion(lightMyopia.getProportion());

        CountAndProportion highMyopia = countAndProportionService.highMyopia(statConclusions);
        table.setHighMyopiaCount(highMyopia.getCount());
        table.setHighMyopiaProportion(highMyopia.getProportion());
        tables.add(table);
        return tables;
    }

    public List<AstigmatismTable> schoolPrimaryRefractiveTable(List<StatConclusion> statConclusions) {
        List<AstigmatismTable> tables = new ArrayList<>();

        Map<Integer, List<StatConclusion>> schoolAgeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));

        schoolAgeMap.forEach((x, y) -> {
            Map<String, List<StatConclusion>> gradeMap = y.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
            gradeMap.forEach((k, v) -> {
                AstigmatismTable table = new AstigmatismTable();
                table.setName(GradeCodeEnum.getName(k));
                table.setValidCount(v.size());

                CountAndProportion myopia = countAndProportionService.myopia(v);
                table.setMyopiaCount(myopia.getCount());
                table.setMyopiaProportion(myopia.getProportion());

                CountAndProportion astigmatism = countAndProportionService.astigmatism(v);
                table.setAstigmatismCount(astigmatism.getCount());
                table.setAstigmatismProportion(astigmatism.getProportion());

                CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(v);
                table.setEarlyMyopiaCount(earlyMyopia.getCount());
                table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

                CountAndProportion lightMyopia = countAndProportionService.lightMyopia(v);
                table.setLightMyopiaCount(lightMyopia.getCount());
                table.setLightMyopiaProportion(lightMyopia.getProportion());

                CountAndProportion highMyopia = countAndProportionService.highMyopia(v);
                table.setHighMyopiaCount(highMyopia.getCount());
                table.setHighMyopiaProportion(highMyopia.getProportion());
                tables.add(table);
            });
            AstigmatismTable table = new AstigmatismTable();
            table.setName(SchoolAge.get(x).desc);
            table.setValidCount(y.size());

            CountAndProportion myopia = countAndProportionService.myopia(y);
            table.setMyopiaCount(myopia.getCount());
            table.setMyopiaProportion(myopia.getProportion());

            CountAndProportion astigmatism = countAndProportionService.astigmatism(y);
            table.setAstigmatismCount(astigmatism.getCount());
            table.setAstigmatismProportion(astigmatism.getProportion());

            CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(y);
            table.setEarlyMyopiaCount(earlyMyopia.getCount());
            table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

            CountAndProportion lightMyopia = countAndProportionService.lightMyopia(y);
            table.setLightMyopiaCount(lightMyopia.getCount());
            table.setLightMyopiaProportion(lightMyopia.getProportion());

            CountAndProportion highMyopia = countAndProportionService.highMyopia(y);
            table.setHighMyopiaCount(highMyopia.getCount());
            table.setHighMyopiaProportion(highMyopia.getProportion());
            tables.add(table);
        });
        AstigmatismTable table = new AstigmatismTable();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());

        CountAndProportion myopia = countAndProportionService.myopia(statConclusions);
        table.setMyopiaCount(myopia.getCount());
        table.setMyopiaProportion(myopia.getProportion());

        CountAndProportion astigmatism = countAndProportionService.astigmatism(statConclusions);
        table.setAstigmatismCount(astigmatism.getCount());
        table.setAstigmatismProportion(astigmatism.getProportion());

        CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(statConclusions);
        table.setEarlyMyopiaCount(earlyMyopia.getCount());
        table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

        CountAndProportion lightMyopia = countAndProportionService.lightMyopia(statConclusions);
        table.setLightMyopiaCount(lightMyopia.getCount());
        table.setLightMyopiaProportion(lightMyopia.getProportion());

        CountAndProportion highMyopia = countAndProportionService.highMyopia(statConclusions);
        table.setHighMyopiaCount(highMyopia.getCount());
        table.setHighMyopiaProportion(highMyopia.getProportion());
        tables.add(table);
        return tables;
    }

    public List<AstigmatismTable> ageAstigmatismTables(List<StatConclusion> statConclusions) {
        List<AstigmatismTable> tables = new ArrayList<>();
        TwoTuple<Integer, Integer> statAgeRange = commonReportService.getStatAgeRange(statConclusions);
        List<AgeGeneration> ageList = AgeGenerationEnum.getPList(statAgeRange.getFirst(), statAgeRange.getSecond());

        ageList.forEach(age -> {
            AstigmatismTable table = new AstigmatismTable();
            List<StatConclusion> list = statConclusions.stream()
                    .filter(s -> s.getAge() >= age.getLeftAge() && s.getAge() < age.getRightAge()).collect(Collectors.toList());

            table.setName(age.getDesc());
            table.setValidCount(list.size());

            CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(list);
            table.setEarlyMyopiaCount(earlyMyopia.getCount());
            table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

            CountAndProportion myopia = countAndProportionService.myopia(list);
            table.setMyopiaCount(myopia.getCount());
            table.setMyopiaProportion(myopia.getProportion());

            CountAndProportion astigmatism = countAndProportionService.astigmatism(list);
            table.setAstigmatismCount(astigmatism.getCount());
            table.setAstigmatismProportion(astigmatism.getProportion());

            CountAndProportion lightMyopia = countAndProportionService.lightMyopia(list);
            table.setLightMyopiaCount(lightMyopia.getCount());
            table.setLightMyopiaProportion(lightMyopia.getProportion());

            CountAndProportion highMyopia = countAndProportionService.highMyopia(list);
            table.setHighMyopiaCount(highMyopia.getCount());
            table.setHighMyopiaProportion(highMyopia.getProportion());
            tables.add(table);
        });

        AstigmatismTable table = new AstigmatismTable();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());

        CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(statConclusions);
        table.setEarlyMyopiaCount(earlyMyopia.getCount());
        table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

        CountAndProportion myopia = countAndProportionService.myopia(statConclusions);
        table.setMyopiaCount(myopia.getCount());
        table.setMyopiaProportion(myopia.getProportion());

        CountAndProportion astigmatism = countAndProportionService.astigmatism(statConclusions);
        table.setAstigmatismCount(astigmatism.getCount());
        table.setAstigmatismProportion(astigmatism.getProportion());

        CountAndProportion lightMyopia = countAndProportionService.lightMyopia(statConclusions);
        table.setLightMyopiaCount(lightMyopia.getCount());
        table.setLightMyopiaProportion(lightMyopia.getProportion());

        CountAndProportion highMyopia = countAndProportionService.highMyopia(statConclusions);
        table.setHighMyopiaCount(highMyopia.getCount());
        table.setHighMyopiaProportion(highMyopia.getProportion());
        tables.add(table);
        return tables;

    }

    public List<AstigmatismTable> pHistoryAstigmatismTable(List<StatConclusion> statConclusions) {

        List<AstigmatismTable> tables = new ArrayList<>();
        List<ChainRatio> historyDate = commonReportService.getHistoryDate(statConclusions);
        historyDate.forEach(s -> {
            AstigmatismTable table = new AstigmatismTable();
            table.setName(s.getName());

            List<StatConclusion> list = statConclusions.stream()
                    .filter(x -> x.getCreateTime().after(s.getStartDate()))
                    .filter(y -> y.getCreateTime().before(s.getEndDate()))
                    .collect(Collectors.toList());

            table.setName(s.getName());
            table.setValidCount(list.size());

            CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(list);
            table.setEarlyMyopiaCount(earlyMyopia.getCount());
            table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

            CountAndProportion myopia = countAndProportionService.myopia(list);
            table.setMyopiaCount(myopia.getCount());
            table.setMyopiaProportion(myopia.getProportion());

            CountAndProportion astigmatism = countAndProportionService.astigmatism(list);
            table.setAstigmatismCount(astigmatism.getCount());
            table.setAstigmatismProportion(astigmatism.getProportion());

            CountAndProportion lightMyopia = countAndProportionService.lightMyopia(list);
            table.setLightMyopiaCount(lightMyopia.getCount());
            table.setLightMyopiaProportion(lightMyopia.getProportion());

            CountAndProportion highMyopia = countAndProportionService.highMyopia(list);
            table.setHighMyopiaCount(highMyopia.getCount());
            table.setHighMyopiaProportion(highMyopia.getProportion());
            tables.add(table);
        });
        return tables;
    }

    public List<AstigmatismTable> gradePrimaryRefractiveTable(List<StatConclusion> statConclusions) {
        List<AstigmatismTable> tables = new ArrayList<>();
        Map<String, List<StatConclusion>> gradeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));

        gradeMap.forEach((k, v) -> {
            AstigmatismTable table = new AstigmatismTable();
            table.setName(GradeCodeEnum.getName(k));
            table.setValidCount(v.size());

            CountAndProportion myopia = countAndProportionService.myopia(v);
            table.setMyopiaCount(myopia.getCount());
            table.setMyopiaProportion(myopia.getProportion());

            CountAndProportion astigmatism = countAndProportionService.astigmatism(v);
            table.setAstigmatismCount(astigmatism.getCount());
            table.setAstigmatismProportion(astigmatism.getProportion());

            CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(v);
            table.setEarlyMyopiaCount(earlyMyopia.getCount());
            table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

            CountAndProportion lightMyopia = countAndProportionService.lightMyopia(v);
            table.setLightMyopiaCount(lightMyopia.getCount());
            table.setLightMyopiaProportion(lightMyopia.getProportion());

            CountAndProportion highMyopia = countAndProportionService.highMyopia(v);
            table.setHighMyopiaCount(highMyopia.getCount());
            table.setHighMyopiaProportion(highMyopia.getProportion());
            tables.add(table);
        });
        AstigmatismTable table = new AstigmatismTable();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());

        CountAndProportion myopia = countAndProportionService.myopia(statConclusions);
        table.setMyopiaCount(myopia.getCount());
        table.setMyopiaProportion(myopia.getProportion());

        CountAndProportion astigmatism = countAndProportionService.astigmatism(statConclusions);
        table.setAstigmatismCount(astigmatism.getCount());
        table.setAstigmatismProportion(astigmatism.getProportion());

        CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(statConclusions);
        table.setEarlyMyopiaCount(earlyMyopia.getCount());
        table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

        CountAndProportion lightMyopia = countAndProportionService.lightMyopia(statConclusions);
        table.setLightMyopiaCount(lightMyopia.getCount());
        table.setLightMyopiaProportion(lightMyopia.getProportion());

        CountAndProportion highMyopia = countAndProportionService.highMyopia(statConclusions);
        table.setHighMyopiaCount(highMyopia.getCount());
        table.setHighMyopiaProportion(highMyopia.getProportion());
        tables.add(table);
        return tables;
    }

    public List<AgeWearingTable> genderWearingTable(List<StatConclusion> statConclusions) {
        List<AgeWearingTable> tables = new ArrayList<>();
        List<Integer> genderList = GenderEnum.genderList();
        Map<Integer, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        genderList.forEach(gender -> {
            AgeWearingTable table = new AgeWearingTable();
            List<StatConclusion> v = collect.getOrDefault(gender, new ArrayList<>());
            table.setName(GenderEnum.getName(gender));
            table.setValidCount(v.size());

            CountAndProportion notWearing = countAndProportionService.notWearing(v);
            table.setNotWearing(notWearing.getCount());
            table.setNotWearingProportion(notWearing.getProportion());

            CountAndProportion glasses = countAndProportionService.glasses(v);
            table.setGlasses(glasses.getCount());
            table.setGlassesProportion(glasses.getProportion());

            CountAndProportion contact = countAndProportionService.contact(v);
            table.setWearingContact(contact.getCount());
            table.setWearingContactProportion(contact.getProportion());

            CountAndProportion night = countAndProportionService.night(v);
            table.setNightWearing(night.getCount());
            table.setNightWearingProportion(night.getProportion());

            CountAndProportion enough = countAndProportionService.enough(v);
            table.setEnough(enough.getCount());
            table.setEnoughProportion(enough.getProportion());

            CountAndProportion uncorrected = countAndProportionService.uncorrected(v);
            table.setUncorrected(uncorrected.getCount());
            table.setUncorrectedProportion(uncorrected.getProportion());

            CountAndProportion under = countAndProportionService.under(v);
            table.setUnder(under.getCount());
            table.setUnderProportion(under.getProportion());
            tables.add(table);
        });

        AgeWearingTable table = new AgeWearingTable();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());

        CountAndProportion notWearing = countAndProportionService.notWearing(statConclusions);
        table.setNotWearing(notWearing.getCount());
        table.setNotWearingProportion(notWearing.getProportion());

        CountAndProportion glasses = countAndProportionService.glasses(statConclusions);
        table.setGlasses(glasses.getCount());
        table.setGlassesProportion(glasses.getProportion());

        CountAndProportion contact = countAndProportionService.contact(statConclusions);
        table.setWearingContact(contact.getCount());
        table.setWearingContactProportion(contact.getProportion());

        CountAndProportion night = countAndProportionService.night(statConclusions);
        table.setNightWearing(night.getCount());
        table.setNightWearingProportion(night.getProportion());

        CountAndProportion enough = countAndProportionService.enough(statConclusions);
        table.setEnough(enough.getCount());
        table.setEnoughProportion(enough.getProportion());

        CountAndProportion uncorrected = countAndProportionService.uncorrected(statConclusions);
        table.setUncorrected(uncorrected.getCount());
        table.setUncorrectedProportion(uncorrected.getProportion());

        CountAndProportion under = countAndProportionService.under(statConclusions);
        table.setUnder(under.getCount());
        table.setUnderProportion(under.getProportion());
        tables.add(table);
        return tables;
    }

    public List<AgeWearingTable> gradeWearingTable(List<StatConclusion> statConclusions) {
        List<AgeWearingTable> tables = new ArrayList<>();
        Map<Integer, List<StatConclusion>> schoolAgeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));

        schoolAgeMap.forEach((x, y) -> {
            Map<String, List<StatConclusion>> collect = y.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
            collect.forEach((k, v) -> {
                AgeWearingTable table = new AgeWearingTable();
                table.setName(GradeCodeEnum.getName(k));
                table.setValidCount(v.size());

                CountAndProportion notWearing = countAndProportionService.notWearing(v);
                table.setNotWearing(notWearing.getCount());
                table.setNotWearingProportion(notWearing.getProportion());

                CountAndProportion glasses = countAndProportionService.glasses(v);
                table.setGlasses(glasses.getCount());
                table.setGlassesProportion(glasses.getProportion());

                CountAndProportion contact = countAndProportionService.contact(v);
                table.setWearingContact(contact.getCount());
                table.setWearingContactProportion(contact.getProportion());

                CountAndProportion night = countAndProportionService.night(v);
                table.setNightWearing(night.getCount());
                table.setNightWearingProportion(night.getProportion());

                CountAndProportion enough = countAndProportionService.enough(v);
                table.setEnough(enough.getCount());
                table.setEnoughProportion(enough.getProportion());

                CountAndProportion uncorrected = countAndProportionService.uncorrected(v);
                table.setUncorrected(uncorrected.getCount());
                table.setUncorrectedProportion(uncorrected.getProportion());

                CountAndProportion under = countAndProportionService.under(v);
                table.setUnder(under.getCount());
                table.setUnderProportion(under.getProportion());
                tables.add(table);
            });
            AgeWearingTable table = new AgeWearingTable();
            table.setName(SchoolAge.get(x).desc);
            table.setValidCount(y.size());

            CountAndProportion notWearing = countAndProportionService.notWearing(y);
            table.setNotWearing(notWearing.getCount());
            table.setNotWearingProportion(notWearing.getProportion());

            CountAndProportion glasses = countAndProportionService.glasses(y);
            table.setGlasses(glasses.getCount());
            table.setGlassesProportion(glasses.getProportion());

            CountAndProportion contact = countAndProportionService.contact(y);
            table.setWearingContact(contact.getCount());
            table.setWearingContactProportion(contact.getProportion());

            CountAndProportion night = countAndProportionService.night(y);
            table.setNightWearing(night.getCount());
            table.setNightWearingProportion(night.getProportion());

            CountAndProportion enough = countAndProportionService.enough(y);
            table.setEnough(enough.getCount());
            table.setEnoughProportion(enough.getProportion());

            CountAndProportion uncorrected = countAndProportionService.uncorrected(y);
            table.setUncorrected(uncorrected.getCount());
            table.setUncorrectedProportion(uncorrected.getProportion());

            CountAndProportion under = countAndProportionService.under(y);
            table.setUnder(under.getCount());
            table.setUnderProportion(under.getProportion());
            tables.add(table);
        });

        AgeWearingTable table = new AgeWearingTable();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());

        CountAndProportion notWearing = countAndProportionService.notWearing(statConclusions);
        table.setNotWearing(notWearing.getCount());
        table.setNotWearingProportion(notWearing.getProportion());

        CountAndProportion glasses = countAndProportionService.glasses(statConclusions);
        table.setGlasses(glasses.getCount());
        table.setGlassesProportion(glasses.getProportion());

        CountAndProportion contact = countAndProportionService.contact(statConclusions);
        table.setWearingContact(contact.getCount());
        table.setWearingContactProportion(contact.getProportion());

        CountAndProportion night = countAndProportionService.night(statConclusions);
        table.setNightWearing(night.getCount());
        table.setNightWearingProportion(night.getProportion());

        CountAndProportion enough = countAndProportionService.enough(statConclusions);
        table.setEnough(enough.getCount());
        table.setEnoughProportion(enough.getProportion());

        CountAndProportion uncorrected = countAndProportionService.uncorrected(statConclusions);
        table.setUncorrected(uncorrected.getCount());
        table.setUncorrectedProportion(uncorrected.getProportion());

        CountAndProportion under = countAndProportionService.under(statConclusions);
        table.setUnder(under.getCount());
        table.setUnderProportion(under.getProportion());
        tables.add(table);

        return tables;
    }

    public List<AgeWearingTable> agePrimaryWearingTable(List<StatConclusion> statConclusions) {
        List<AgeWearingTable> tables = new ArrayList<>();
        TwoTuple<Integer, Integer> statAgeRange = commonReportService.getStatAgeRange(statConclusions);
        List<AgeGeneration> ageList = AgeGenerationEnum.getPList(statAgeRange.getFirst(), statAgeRange.getSecond());

        ageList.forEach(age -> {
            AgeWearingTable table = new AgeWearingTable();
            table.setName(age.getDesc());
            List<StatConclusion> list = statConclusions.stream()
                    .filter(s -> s.getAge() >= age.getLeftAge() && s.getAge() < age.getRightAge()).collect(Collectors.toList());
            table.setValidCount(list.size());

            CountAndProportion notWearing = countAndProportionService.notWearing(list);
            table.setNotWearing(notWearing.getCount());
            table.setNotWearingProportion(notWearing.getProportion());

            CountAndProportion glasses = countAndProportionService.glasses(list);
            table.setGlasses(glasses.getCount());
            table.setGlassesProportion(glasses.getProportion());

            CountAndProportion contact = countAndProportionService.contact(list);
            table.setWearingContact(contact.getCount());
            table.setWearingContactProportion(contact.getProportion());

            CountAndProportion night = countAndProportionService.night(list);
            table.setNightWearing(night.getCount());
            table.setNightWearingProportion(night.getProportion());

            CountAndProportion enough = countAndProportionService.enough(list);
            table.setEnough(enough.getCount());
            table.setEnoughProportion(enough.getProportion());

            CountAndProportion uncorrected = countAndProportionService.uncorrected(list);
            table.setUncorrected(uncorrected.getCount());
            table.setUncorrectedProportion(uncorrected.getProportion());

            CountAndProportion under = countAndProportionService.under(list);
            table.setUnder(under.getCount());
            table.setUnderProportion(under.getProportion());
            tables.add(table);
        });

        AgeWearingTable table = new AgeWearingTable();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());

        CountAndProportion notWearing = countAndProportionService.notWearing(statConclusions);
        table.setNotWearing(notWearing.getCount());
        table.setNotWearingProportion(notWearing.getProportion());

        CountAndProportion glasses = countAndProportionService.glasses(statConclusions);
        table.setGlasses(glasses.getCount());
        table.setGlassesProportion(glasses.getProportion());

        CountAndProportion contact = countAndProportionService.contact(statConclusions);
        table.setWearingContact(contact.getCount());
        table.setWearingContactProportion(contact.getProportion());

        CountAndProportion night = countAndProportionService.night(statConclusions);
        table.setNightWearing(night.getCount());
        table.setNightWearingProportion(night.getProportion());

        CountAndProportion enough = countAndProportionService.enough(statConclusions);
        table.setEnough(enough.getCount());
        table.setEnoughProportion(enough.getProportion());

        CountAndProportion uncorrected = countAndProportionService.uncorrected(statConclusions);
        table.setUncorrected(uncorrected.getCount());
        table.setUncorrectedProportion(uncorrected.getProportion());

        CountAndProportion under = countAndProportionService.under(statConclusions);
        table.setUnder(under.getCount());
        table.setUnderProportion(under.getProportion());
        tables.add(table);
        return tables;
    }

    public List<AgeWearingTable> gradePrimaryWearingTable(List<StatConclusion> statConclusions) {
        List<AgeWearingTable> tables = new ArrayList<>();
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        collect.forEach((k, v) -> {
            AgeWearingTable table = new AgeWearingTable();
            table.setName(GradeCodeEnum.getName(k));
            table.setValidCount(v.size());

            CountAndProportion notWearing = countAndProportionService.notWearing(v);
            table.setNotWearing(notWearing.getCount());
            table.setNotWearingProportion(notWearing.getProportion());

            CountAndProportion glasses = countAndProportionService.glasses(v);
            table.setGlasses(glasses.getCount());
            table.setGlassesProportion(glasses.getProportion());

            CountAndProportion contact = countAndProportionService.contact(v);
            table.setWearingContact(contact.getCount());
            table.setWearingContactProportion(contact.getProportion());

            CountAndProportion night = countAndProportionService.night(v);
            table.setNightWearing(night.getCount());
            table.setNightWearingProportion(night.getProportion());

            CountAndProportion enough = countAndProportionService.enough(v);
            table.setEnough(enough.getCount());
            table.setEnoughProportion(enough.getProportion());

            CountAndProportion uncorrected = countAndProportionService.uncorrected(v);
            table.setUncorrected(uncorrected.getCount());
            table.setUncorrectedProportion(uncorrected.getProportion());

            CountAndProportion under = countAndProportionService.under(v);
            table.setUnder(under.getCount());
            table.setUnderProportion(under.getProportion());
            tables.add(table);
        });
        AgeWearingTable table = new AgeWearingTable();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());

        CountAndProportion notWearing = countAndProportionService.notWearing(statConclusions);
        table.setNotWearing(notWearing.getCount());
        table.setNotWearingProportion(notWearing.getProportion());

        CountAndProportion glasses = countAndProportionService.glasses(statConclusions);
        table.setGlasses(glasses.getCount());
        table.setGlassesProportion(glasses.getProportion());

        CountAndProportion contact = countAndProportionService.contact(statConclusions);
        table.setWearingContact(contact.getCount());
        table.setWearingContactProportion(contact.getProportion());

        CountAndProportion night = countAndProportionService.night(statConclusions);
        table.setNightWearing(night.getCount());
        table.setNightWearingProportion(night.getProportion());

        CountAndProportion enough = countAndProportionService.enough(statConclusions);
        table.setEnough(enough.getCount());
        table.setEnoughProportion(enough.getProportion());

        CountAndProportion uncorrected = countAndProportionService.uncorrected(statConclusions);
        table.setUncorrected(uncorrected.getCount());
        table.setUncorrectedProportion(uncorrected.getProportion());

        CountAndProportion under = countAndProportionService.under(statConclusions);
        table.setUnder(under.getCount());
        table.setUnderProportion(under.getProportion());
        tables.add(table);

        tables.add(table);
        return tables;
    }

    public List<WarningTable> warningTable(List<StatConclusion> statConclusions) {
        List<WarningTable> tables = new ArrayList<>();
        Map<Integer, List<StatConclusion>> schoolAgeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));

        schoolAgeMap.forEach((x, y) -> {
            Map<String, List<StatConclusion>> collect = y.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
            collect.forEach((k, v) -> {
                WarningTable table = new WarningTable();
                table.setName(GradeCodeEnum.getName(k));
                table.setValidCount(v.size());
                CountAndProportion zero = countAndProportionService.zeroWarning(v);
                table.setZeroWarningCount(zero.getCount());
                table.setZeroWarningProportion(zero.getProportion());

                CountAndProportion one = countAndProportionService.oneWarning(v);
                table.setOneWarningCount(one.getCount());
                table.setOneWarningProportion(one.getProportion());

                CountAndProportion two = countAndProportionService.twoWarning(v);
                table.setTwoWarningCount(two.getCount());
                table.setTwoWarningProportion(two.getProportion());

                CountAndProportion threeWarning = countAndProportionService.threeWarning(v);
                table.setThreeWarningCount(threeWarning.getCount());
                table.setThreeWarningProportion(threeWarning.getProportion());

                CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(v);
                table.setRecommendDoctorCount(recommendDoctor.getCount());
                table.setRecommendDoctorProportion(recommendDoctor.getProportion());
                tables.add(table);
            });
            WarningTable table = new WarningTable();
            table.setName(SchoolAge.get(x).desc);
            table.setValidCount(y.size());
            CountAndProportion zero = countAndProportionService.zeroWarning(y);
            table.setZeroWarningCount(zero.getCount());
            table.setZeroWarningProportion(zero.getProportion());

            CountAndProportion one = countAndProportionService.oneWarning(y);
            table.setOneWarningCount(one.getCount());
            table.setOneWarningProportion(one.getProportion());

            CountAndProportion two = countAndProportionService.twoWarning(y);
            table.setTwoWarningCount(two.getCount());
            table.setTwoWarningProportion(two.getProportion());

            CountAndProportion threeWarning = countAndProportionService.threeWarning(y);
            table.setThreeWarningCount(threeWarning.getCount());
            table.setThreeWarningProportion(threeWarning.getProportion());

            CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(y);
            table.setRecommendDoctorCount(recommendDoctor.getCount());
            table.setRecommendDoctorProportion(recommendDoctor.getProportion());
            tables.add(table);
        });
        WarningTable table = new WarningTable();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());
        CountAndProportion zero = countAndProportionService.zeroWarning(statConclusions);
        table.setZeroWarningCount(zero.getCount());
        table.setZeroWarningProportion(zero.getProportion());

        CountAndProportion one = countAndProportionService.oneWarning(statConclusions);
        table.setOneWarningCount(one.getCount());
        table.setOneWarningProportion(one.getProportion());

        CountAndProportion two = countAndProportionService.twoWarning(statConclusions);
        table.setTwoWarningCount(two.getCount());
        table.setTwoWarningProportion(two.getProportion());

        CountAndProportion threeWarning = countAndProportionService.threeWarning(statConclusions);
        table.setThreeWarningCount(threeWarning.getCount());
        table.setThreeWarningProportion(threeWarning.getProportion());

        CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(statConclusions);
        table.setRecommendDoctorCount(recommendDoctor.getCount());
        table.setRecommendDoctorProportion(recommendDoctor.getProportion());
        tables.add(table);
        return tables;
    }

    public List<KindergartenScreeningInfoTable> kindergartenScreeningInfoTables(List<StatConclusion> statConclusions) {
        List<KindergartenScreeningInfoTable> tables = new ArrayList<>();
        List<Integer> schoolIds = statConclusions.stream().map(StatConclusion::getSchoolId).collect(Collectors.toList());
        Map<Integer, String> schoolMap = schoolService.getByIds(schoolIds).stream().collect(Collectors.toMap(School::getId, School::getName));

        Map<Integer, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
        collect.forEach((k, v) -> {
            KindergartenScreeningInfoTable table = new KindergartenScreeningInfoTable();
            table.setName(schoolMap.get(k));
            table.setValidCount(v.size());

            CountAndProportion lowVision = countAndProportionService.lowVision(v);
            table.setLowVisionCount(lowVision.getCount());
            table.setLowVisionProportion(lowVision.getProportion());

            table.setAvgVision(commonReportService.getAvgVision(v));

            CountAndProportion insufficient = countAndProportionService.insufficient(v);
            table.setInsufficientCount(insufficient.getCount());
            table.setInsufficientProportion(insufficient.getProportion());

            CountAndProportion refractiveError = countAndProportionService.refractiveError(v);
            table.setRefractiveErrorCount(refractiveError.getCount());
            table.setRefractiveErrorProportion(refractiveError.getProportion());

            CountAndProportion anisometropia = countAndProportionService.anisometropia(v);
            table.setAnisometropiaCount(anisometropia.getCount());
            table.setAnisometropiaProportion(anisometropia.getProportion());

            CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(v);
            table.setRecommendDoctorCount(recommendDoctor.getCount());
            table.setRecommendDoctorProportion(recommendDoctor.getProportion());
            tables.add(table);
        });
        return tables.stream().sorted(Comparator.comparing(KindergartenScreeningInfoTable::getLowVisionProportion).reversed()).collect(Collectors.toList());
    }

    public List<PrimaryScreeningInfoTable> schoolScreeningInfoTables(List<StatConclusion> statConclusions) {
        List<PrimaryScreeningInfoTable> tables = new ArrayList<>();
        List<Integer> schoolIds = statConclusions.stream().map(StatConclusion::getSchoolId).collect(Collectors.toList());
        Map<Integer, String> schoolMap = schoolService.getByIds(schoolIds).stream().collect(Collectors.toMap(School::getId, School::getName));

        Map<Integer, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
        collect.forEach((k, v) -> {
            PrimaryScreeningInfoTable table = new PrimaryScreeningInfoTable();
            table.setName(schoolMap.get(k));
            table.setValidCount(v.size());

            CountAndProportion lowVision = countAndProportionService.lowVision(v);
            table.setLowVisionCount(lowVision.getCount());
            table.setLowVisionProportion(lowVision.getProportion());

            table.setAvgVision(commonReportService.getAvgVision(v));

            CountAndProportion myopia = countAndProportionService.myopia(v);
            table.setMyopiaCount(myopia.getCount());
            table.setMyopiaProportion(myopia.getProportion());

            CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(v);
            table.setEarlyMyopiaCount(earlyMyopia.getCount());
            table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

            CountAndProportion lightMyopia = countAndProportionService.lightMyopia(v);
            table.setLightMyopiaCount(lightMyopia.getCount());
            table.setLightMyopiaProportion(lightMyopia.getProportion());

            CountAndProportion highMyopia = countAndProportionService.highMyopia(v);
            table.setHighMyopiaCount(highMyopia.getCount());
            table.setHighMyopiaProportion(highMyopia.getProportion());

            CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(v);
            table.setRecommendDoctorCount(recommendDoctor.getCount());
            table.setRecommendDoctorProportion(recommendDoctor.getProportion());

            CountAndProportion underAndUncorrected = countAndProportionService.underAndUncorrected(v);
            table.setOweCount(underAndUncorrected.getCount());
            table.setOweProportion(underAndUncorrected.getProportion());
            tables.add(table);
        });
        return tables;
    }

    public List<PrimaryScreeningInfoTable> primaryScreeningInfoTables(List<StatConclusion> statConclusions) {
        List<PrimaryScreeningInfoTable> tables = new ArrayList<>();

        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        collect.forEach((k, v) -> {
            PrimaryScreeningInfoTable table = new PrimaryScreeningInfoTable();
            table.setName(GradeCodeEnum.getName(k));
            table.setValidCount(v.size());

            CountAndProportion lowVision = countAndProportionService.lowVision(v);
            table.setLowVisionCount(lowVision.getCount());
            table.setLowVisionProportion(lowVision.getProportion());

            table.setAvgVision(commonReportService.getAvgVision(v));

            CountAndProportion myopia = countAndProportionService.myopia(v);
            table.setMyopiaCount(myopia.getCount());
            table.setMyopiaProportion(myopia.getProportion());

            CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(v);
            table.setEarlyMyopiaCount(earlyMyopia.getCount());
            table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

            CountAndProportion lightMyopia = countAndProportionService.lightMyopia(v);
            table.setLightMyopiaCount(lightMyopia.getCount());
            table.setLightMyopiaProportion(lightMyopia.getProportion());

            CountAndProportion highMyopia = countAndProportionService.highMyopia(v);
            table.setHighMyopiaCount(highMyopia.getCount());
            table.setHighMyopiaProportion(highMyopia.getProportion());

            CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(v);
            table.setRecommendDoctorCount(recommendDoctor.getCount());
            table.setRecommendDoctorProportion(recommendDoctor.getProportion());

            CountAndProportion underAndUncorrected = countAndProportionService.underAndUncorrected(v);
            table.setOweCount(underAndUncorrected.getCount());
            table.setOweProportion(underAndUncorrected.getProportion());
            tables.add(table);
        });
        return tables;
    }


    public List<SchoolHistoryLowVisionTable> historySchoolAgeLowVisionTables(List<StatConclusion> statConclusions) {
        List<SchoolHistoryLowVisionTable> tables = new ArrayList<>();
        List<ChainRatio> historyDate = commonReportService.getHistoryDate(statConclusions);
        historyDate.forEach(s -> {
            SchoolHistoryLowVisionTable table = new SchoolHistoryLowVisionTable();
            table.setName(s.getName());

            List<StatConclusion> list = statConclusions.stream()
                    .filter(x -> x.getCreateTime().after(s.getStartDate()))
                    .filter(y -> y.getCreateTime().before(s.getEndDate()))
                    .collect(Collectors.toList());

            table.setValidCount(list.size());

            List<StatConclusion> kList = commonReportService.getKList(list);
            List<StatConclusion> pList = commonReportService.getPList(list);

            CountAndProportion kLowVision = countAndProportionService.lowVision(kList);
            table.setKLowVisionCount(kLowVision.getCount());
            table.setKLowVisionProportion(kLowVision.getProportion());

            CountAndProportion lowVision = countAndProportionService.lowVision(pList);
            table.setLowVisionCount(lowVision.getCount());
            table.setLowVisionProportion(lowVision.getProportion());

            CountAndProportion lightLowVision = countAndProportionService.lightLowVision(pList);
            table.setLightLowVisionCount(lightLowVision.getCount());
            table.setLightLowVisionProportion(lightLowVision.getProportion());

            CountAndProportion middleLowVision = countAndProportionService.middleLowVision(pList);
            table.setMiddleLowVisionCount(middleLowVision.getCount());
            table.setMiddleLowVisionProportion(middleLowVision.getProportion());

            CountAndProportion highLowVision = countAndProportionService.highLowVision(pList);
            table.setHighLowVisionCount(highLowVision.getCount());
            table.setHighLowVisionProportion(highLowVision.getProportion());
            tables.add(table);
        });
        return tables;
    }

    /**
     * 学龄段性别统计表格
     */
    public List<SchoolAgeGenderTable> areaOutlineTable(List<StatConclusion> statConclusions) {
        List<SchoolAgeGenderTable> tables = new ArrayList<>();
        List<Integer> schoolAges = statConclusions.stream().map(StatConclusion::getSchoolAge).distinct().collect(Collectors.toList());

        for (Integer schoolAge : schoolAges) {
            SchoolAgeGenderTable schoolAgeGenderTable = new SchoolAgeGenderTable();
            schoolAgeGenderTable.setName(SchoolAge.get(schoolAge).desc);
            List<StatConclusion> list = statConclusions.stream().filter(s -> Objects.equals(s.getSchoolAge(), schoolAge)).collect(Collectors.toList());

            schoolAgeGenderTable.setMCount(list.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.MALE.type)).count());
            schoolAgeGenderTable.setMValidCount(list.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.MALE.type)).filter(StatConclusion::getIsValid).count());

            schoolAgeGenderTable.setFCount(list.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.FEMALE.type)).count());
            schoolAgeGenderTable.setFValidCount(list.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.FEMALE.type)).filter(StatConclusion::getIsValid).count());

            schoolAgeGenderTable.setTotalCount((long) list.size());
            schoolAgeGenderTable.setTotalValidCount(list.stream().filter(StatConclusion::getIsValid).count());
            schoolAgeGenderTable.setTotalValidProportion(BigDecimalUtil.divide(schoolAgeGenderTable.getTotalValidCount(), schoolAgeGenderTable.getTotalCount()));
            tables.add(schoolAgeGenderTable);
        }
        SchoolAgeGenderTable total = new SchoolAgeGenderTable();
        total.setName(CommonReportService.TOTAL_NAME);
        List<StatConclusion> mList = statConclusions.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.MALE.type)).collect(Collectors.toList());
        List<StatConclusion> fList = statConclusions.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.FEMALE.type)).collect(Collectors.toList());

        total.setMCount((long) mList.size());
        total.setMValidCount(mList.stream().filter(StatConclusion::getIsValid).count());

        total.setFCount((long) fList.size());
        total.setFValidCount(fList.stream().filter(StatConclusion::getIsValid).count());

        total.setTotalCount((long) statConclusions.size());
        total.setTotalValidCount(statConclusions.stream().filter(StatConclusion::getIsValid).count());
        total.setTotalValidProportion(BigDecimalUtil.divide(total.getTotalValidCount(), total.getTotalCount()));
        tables.add(total);
        return tables;
    }

    public List<GenderSexLowVisionTable> genderSexLowVisionTable(List<StatConclusion> statConclusions) {
        // 表格
        List<GenderSexLowVisionTable> tables = new ArrayList<>();
        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<Integer> genderList = GenderEnum.genderList();
        genderList.forEach(gender -> {
            GenderSexLowVisionTable table = new GenderSexLowVisionTable();
            List<StatConclusion> v = genderMap.getOrDefault(gender, new ArrayList<>());
            table.setName(GenderEnum.getName(gender));
            table.setValidCount(commonReportService.getValidList(v).size());

            CountAndProportion kLowVision = countAndProportionService.lowVision(commonReportService.getKList(v));
            CountAndProportion pLowVision = countAndProportionService.lowVision(commonReportService.getKList(v));

            table.setKCount(kLowVision.getCount());
            table.setKProportion(kLowVision.getProportion());
            table.setKAvgVision(commonReportService.getAvgVision(commonReportService.getKList(v)));
            table.setPCount(pLowVision.getCount());
            table.setPProportion(pLowVision.getProportion());
            table.setPAvgVision(commonReportService.getAvgVision(commonReportService.getPList(v)));

            tables.add(table);
        });
        GenderSexLowVisionTable total = new GenderSexLowVisionTable();
        total.setName(CommonReportService.TOTAL_NAME);
        total.setValidCount(statConclusions.size());
        CountAndProportion kLowVision = countAndProportionService.lowVision(commonReportService.getKList(statConclusions));
        CountAndProportion pLowVision = countAndProportionService.lowVision(commonReportService.getKList(statConclusions));

        total.setKCount(kLowVision.getCount());
        total.setKProportion(kLowVision.getProportion());
        total.setKAvgVision(commonReportService.getAvgVision(commonReportService.getKList(statConclusions)));
        total.setPCount(pLowVision.getCount());
        total.setPProportion(pLowVision.getProportion());
        total.setPAvgVision(commonReportService.getAvgVision(commonReportService.getPList(statConclusions)));
        tables.add(total);
        return tables;
    }

    // -------------------------------------------小学--------------------------------------------------------------------
    public List<MyopiaTable> historyMyopiaTables(List<StatConclusion> statConclusions) {
        List<MyopiaTable> tables = new ArrayList<>();

        List<ChainRatio> historyDate = commonReportService.getHistoryDate(statConclusions);
        historyDate.forEach(s -> {
            MyopiaTable table = new MyopiaTable();

            List<StatConclusion> list = statConclusions.stream()
                    .filter(x -> x.getCreateTime().after(s.getStartDate()))
                    .filter(y -> y.getCreateTime().before(s.getEndDate()))
                    .collect(Collectors.toList());

            CountAndProportion lowVision = countAndProportionService.lowVision(list);
            table.setLowVision(lowVision.getCount());
            table.setLowVisionProportion(lowVision.getProportion());

            CountAndProportion myopia = countAndProportionService.myopia(list);
            table.setMyopia(myopia.getCount());
            table.setMyopiaProportion(myopia.getProportion());

            CountAndProportion early = countAndProportionService.earlyMyopia(list);
            table.setEarly(early.getCount());
            table.setEarlyProportion(early.getProportion());

            CountAndProportion light = countAndProportionService.lightMyopia(list);
            table.setLight(light.getCount());
            table.setLightProportion(light.getProportion());

            CountAndProportion middle = countAndProportionService.middleMyopia(list);
            table.setMiddle(middle.getCount());
            table.setMiddleProportion(middle.getProportion());

            CountAndProportion high = countAndProportionService.highMyopia(list);
            table.setHigh(high.getCount());
            table.setHighProportion(high.getProportion());

            table.setName(s.getName());
            table.setValidCount(list.size());

            tables.add(table);
        });
        return tables;
    }

    public List<LowVisionTable> lowVisionTables(List<StatConclusion> statConclusions) {
        List<LowVisionTable> tables = new ArrayList<>();
        Map<Integer, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));

        List<Integer> genderList = GenderEnum.genderList();
        genderList.forEach(gender -> {
            LowVisionTable lowVisionTable = new LowVisionTable();
            lowVisionTable.setName(GenderEnum.getName(gender));
            List<StatConclusion> v = collect.getOrDefault(gender, new ArrayList<>());
            lowVisionTable.setValidCount(v.size());

            CountAndProportion lowVision = countAndProportionService.lowVision(v);
            lowVisionTable.setLowVisionCount(lowVision.getCount());
            lowVisionTable.setLowVisionProportion(lowVision.getProportion());

            CountAndProportion light = countAndProportionService.lightLowVision(v);
            lowVisionTable.setLightVisionCount(light.getCount());
            lowVisionTable.setLightVisionProportion(light.getProportion());

            CountAndProportion middle = countAndProportionService.middleLowVision(v);
            lowVisionTable.setMiddleVisionCount(middle.getCount());
            lowVisionTable.setMiddleVisionProportion(middle.getProportion());

            CountAndProportion high = countAndProportionService.highLowVision(v);
            lowVisionTable.setHighVisionCount(high.getCount());
            lowVisionTable.setHighVisionProportion(high.getProportion());
            lowVisionTable.setAvgVision(commonReportService.getAvgVision(v));
            tables.add(lowVisionTable);
        });

        LowVisionTable lowVisionTable = new LowVisionTable();
        lowVisionTable.setName(CommonReportService.TOTAL_NAME);
        lowVisionTable.setValidCount(statConclusions.size());

        CountAndProportion lowVision = countAndProportionService.lowVision(statConclusions);
        lowVisionTable.setLowVisionCount(lowVision.getCount());
        lowVisionTable.setLowVisionProportion(lowVision.getProportion());

        CountAndProportion light = countAndProportionService.lightLowVision(statConclusions);
        lowVisionTable.setLightVisionCount(light.getCount());
        lowVisionTable.setLightVisionProportion(light.getProportion());

        CountAndProportion middle = countAndProportionService.middleLowVision(statConclusions);
        lowVisionTable.setMiddleVisionCount(middle.getCount());
        lowVisionTable.setMiddleVisionProportion(middle.getProportion());

        CountAndProportion high = countAndProportionService.highLowVision(statConclusions);
        lowVisionTable.setHighVisionCount(high.getCount());
        lowVisionTable.setHighVisionProportion(high.getProportion());
        lowVisionTable.setAvgVision(commonReportService.getAvgVision(statConclusions));
        tables.add(lowVisionTable);

        return tables;
    }

    public List<LowVisionTable> gradeLowVision(List<StatConclusion> statConclusions) {
        List<LowVisionTable> tables = new ArrayList<>();
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        collect.forEach((k, v) -> {
            LowVisionTable lowVisionTable = new LowVisionTable();
            lowVisionTable.setName(GradeCodeEnum.getName(k));
            lowVisionTable.setValidCount(v.size());

            CountAndProportion lowVision = countAndProportionService.lowVision(v);
            lowVisionTable.setLowVisionCount(lowVision.getCount());
            lowVisionTable.setLowVisionProportion(lowVision.getProportion());

            CountAndProportion light = countAndProportionService.lightLowVision(v);
            lowVisionTable.setLightVisionCount(light.getCount());
            lowVisionTable.setLightVisionProportion(light.getProportion());

            CountAndProportion middle = countAndProportionService.middleLowVision(v);
            lowVisionTable.setMiddleVisionCount(middle.getCount());
            lowVisionTable.setMiddleVisionProportion(middle.getProportion());

            CountAndProportion high = countAndProportionService.highLowVision(v);
            lowVisionTable.setHighVisionCount(high.getCount());
            lowVisionTable.setHighVisionProportion(high.getProportion());
            lowVisionTable.setAvgVision(commonReportService.getAvgVision(v));
            tables.add(lowVisionTable);
        });

        LowVisionTable lowVisionTable = new LowVisionTable();
        lowVisionTable.setName(CommonReportService.TOTAL_NAME);
        lowVisionTable.setValidCount(statConclusions.size());

        CountAndProportion lowVision = countAndProportionService.lowVision(statConclusions);
        lowVisionTable.setLowVisionCount(lowVision.getCount());
        lowVisionTable.setLowVisionProportion(lowVision.getProportion());

        CountAndProportion light = countAndProportionService.lightLowVision(statConclusions);
        lowVisionTable.setLightVisionCount(light.getCount());
        lowVisionTable.setLightVisionProportion(light.getProportion());

        CountAndProportion middle = countAndProportionService.middleLowVision(statConclusions);
        lowVisionTable.setMiddleVisionCount(middle.getCount());
        lowVisionTable.setMiddleVisionProportion(middle.getProportion());

        CountAndProportion high = countAndProportionService.highLowVision(statConclusions);
        lowVisionTable.setHighVisionCount(high.getCount());
        lowVisionTable.setHighVisionProportion(high.getProportion());
        lowVisionTable.setAvgVision(commonReportService.getAvgVision(statConclusions));
        tables.add(lowVisionTable);

        return tables;
    }

    public List<LowVisionTable> ageLowTable(List<StatConclusion> statConclusions) {
        List<LowVisionTable> tables = new ArrayList<>();

        TwoTuple<Integer, Integer> statAgeRange = commonReportService.getStatAgeRange(statConclusions);
        List<AgeGeneration> ageList = AgeGenerationEnum.getPList(statAgeRange.getFirst(), statAgeRange.getSecond());

        ageList.forEach(age -> {
            LowVisionTable table = new LowVisionTable();
            table.setName(age.getDesc());
            List<StatConclusion> list = statConclusions.stream()
                    .filter(s -> s.getAge() >= age.getLeftAge() && s.getAge() < age.getRightAge()).collect(Collectors.toList());
            table.setValidCount(list.size());

            LowVisionTable lowVisionTable = new LowVisionTable();
            lowVisionTable.setName(age.getDesc());
            lowVisionTable.setValidCount(list.size());

            CountAndProportion lowVision = countAndProportionService.lowVision(list);
            lowVisionTable.setLowVisionCount(lowVision.getCount());
            lowVisionTable.setLowVisionProportion(lowVision.getProportion());

            CountAndProportion light = countAndProportionService.lightLowVision(list);
            lowVisionTable.setLightVisionCount(light.getCount());
            lowVisionTable.setLightVisionProportion(light.getProportion());

            CountAndProportion middle = countAndProportionService.middleLowVision(list);
            lowVisionTable.setMiddleVisionCount(middle.getCount());
            lowVisionTable.setMiddleVisionProportion(middle.getProportion());

            CountAndProportion high = countAndProportionService.highLowVision(list);
            lowVisionTable.setHighVisionCount(high.getCount());
            lowVisionTable.setHighVisionProportion(high.getProportion());
            lowVisionTable.setAvgVision(commonReportService.getAvgVision(list));
            tables.add(lowVisionTable);
        });

        LowVisionTable lowVisionTable = new LowVisionTable();
        lowVisionTable.setName(CommonReportService.TOTAL_NAME);
        lowVisionTable.setValidCount(statConclusions.size());

        CountAndProportion lowVision = countAndProportionService.lowVision(statConclusions);
        lowVisionTable.setLowVisionCount(lowVision.getCount());
        lowVisionTable.setLowVisionProportion(lowVision.getProportion());

        CountAndProportion light = countAndProportionService.lightLowVision(statConclusions);
        lowVisionTable.setLightVisionCount(light.getCount());
        lowVisionTable.setLightVisionProportion(light.getProportion());

        CountAndProportion middle = countAndProportionService.middleLowVision(statConclusions);
        lowVisionTable.setMiddleVisionCount(middle.getCount());
        lowVisionTable.setMiddleVisionProportion(middle.getProportion());

        CountAndProportion high = countAndProportionService.highLowVision(statConclusions);
        lowVisionTable.setHighVisionCount(high.getCount());
        lowVisionTable.setHighVisionProportion(high.getProportion());
        lowVisionTable.setAvgVision(commonReportService.getAvgVision(statConclusions));
        tables.add(lowVisionTable);
        return tables;
    }


    // -------------------------------------------------幼儿园---------------------------------------------------------
    public List<SexLowVision.Table> sexLowVisionTable(List<StatConclusion> statConclusions) {
        List<SexLowVision.Table> tables = new ArrayList<>();
        List<Integer> genderList = GenderEnum.genderList();
        Map<Integer, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        genderList.forEach(gender -> {
            SexLowVision.Table table = new SexLowVision.Table();
            table.setName(GenderEnum.getName(gender));
            List<StatConclusion> v = collect.getOrDefault(gender, new ArrayList<>());
            table.setValidCount(commonReportService.getValidList(v).size());
            CountAndProportion lowVision = countAndProportionService.lowVision(v);
            table.setLowVisionCount(lowVision.getCount());
            table.setProportion(lowVision.getProportion());
            table.setAvgVision(commonReportService.getAvgVision(v));
            tables.add(table);
        });
        SexLowVision.Table table = new SexLowVision.Table();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setValidCount(commonReportService.getValidList(statConclusions).size());
        CountAndProportion lowVision = countAndProportionService.lowVision(statConclusions);
        table.setLowVisionCount(lowVision.getCount());
        table.setProportion(lowVision.getProportion());
        table.setAvgVision(commonReportService.getAvgVision(statConclusions));
        tables.add(table);
        return tables;
    }

    public List<GradeRefractive.Table> gradeRefractiveTables(List<StatConclusion> statConclusions) {
        List<GradeRefractive.Table> tables = new ArrayList<>();
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        List<String> kindergartenCodeList = GradeCodeEnum.kindergartenSchoolCode();
        kindergartenCodeList.forEach(code -> {
            List<StatConclusion> list = collect.getOrDefault(code, null);
            if (Objects.isNull(list)) {
                return;
            }
            Map<String, List<StatConclusion>> classList = list.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolClassName));
            classList.forEach((k, v) -> {
                GradeRefractive.Table table = new GradeRefractive.Table();
                table.setName(GradeCodeEnum.getName(code));
                table.setClassName(k);
                table.setValidCount(v.size());
                CountAndProportion insufficient = countAndProportionService.insufficient(v);
                table.setInsufficientStudentCount(insufficient.getCount());
                table.setInsufficientProportion(insufficient.getProportion());
                CountAndProportion refractiveError = countAndProportionService.refractiveError(v);
                table.setRefractiveErrorStudentCount(refractiveError.getCount());
                table.setRefractiveErrorProportion(refractiveError.getProportion());
                CountAndProportion anisometropia = countAndProportionService.anisometropia(v);
                table.setAnisometropiaStudentCount(anisometropia.getCount());
                table.setAnisometropiaProportion(anisometropia.getProportion());
                tables.add(table);
            });
        });
        GradeRefractive.Table table = new GradeRefractive.Table();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setClassName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());
        CountAndProportion insufficient = countAndProportionService.insufficient(statConclusions);
        table.setInsufficientStudentCount(insufficient.getCount());
        table.setInsufficientProportion(insufficient.getProportion());
        CountAndProportion refractiveError = countAndProportionService.refractiveError(statConclusions);
        table.setRefractiveErrorStudentCount(refractiveError.getCount());
        table.setRefractiveErrorProportion(refractiveError.getProportion());
        CountAndProportion anisometropia = countAndProportionService.anisometropia(statConclusions);
        table.setAnisometropiaStudentCount(anisometropia.getCount());
        table.setAnisometropiaProportion(anisometropia.getProportion());
        tables.add(table);
        return tables;
    }

    public List<GradeWarning.Table> gradeWarningTable(List<StatConclusion> statConclusions) {
        List<GradeWarning.Table> tables = new ArrayList<>();
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        List<String> kindergartenCodeList = GradeCodeEnum.kindergartenSchoolCode();
        kindergartenCodeList.forEach(code -> {
            List<StatConclusion> list = collect.getOrDefault(code, null);
            if (Objects.isNull(list)) {
                return;
            }
            Map<String, List<StatConclusion>> classList = list.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolClassName));
            classList.forEach((k, v) -> {
                GradeWarning.Table table = new GradeWarning.Table();
                table.setName(GradeCodeEnum.getName(code));
                table.setClassName(k);
                table.setValidCount(v.size());
                CountAndProportion zeroWarning = countAndProportionService.zeroWarning(v);
                table.setZeroWarningCount(zeroWarning.getCount());
                table.setZeroWarningProportion(zeroWarning.getProportion());
                CountAndProportion oneWarning = countAndProportionService.oneWarning(v);
                table.setOneWarningCount(oneWarning.getCount());
                table.setOneWarningProportion(oneWarning.getProportion());
                CountAndProportion twoWarning = countAndProportionService.twoWarning(v);
                table.setTwoWarningCount(twoWarning.getCount());
                table.setTwoWarningProportion(twoWarning.getProportion());
                CountAndProportion threeWarning = countAndProportionService.threeWarning(v);
                table.setThreeWarningCount(threeWarning.getCount());
                table.setThreeWarningProportion(threeWarning.getProportion());
                CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(v);
                table.setRecommendDoctorCount(recommendDoctor.getCount());
                table.setRecommendDoctorProportion(recommendDoctor.getProportion());
                CountAndProportion warning = countAndProportionService.warning(v);
                table.setWarningCount(warning.getCount());
                table.setWarningProportion(warning.getProportion());
                tables.add(table);
            });
        });
        GradeWarning.Table table = new GradeWarning.Table();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setClassName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());
        CountAndProportion zeroWarning = countAndProportionService.zeroWarning(statConclusions);
        table.setZeroWarningCount(zeroWarning.getCount());
        table.setZeroWarningProportion(zeroWarning.getProportion());
        CountAndProportion oneWarning = countAndProportionService.oneWarning(statConclusions);
        table.setOneWarningCount(oneWarning.getCount());
        table.setOneWarningProportion(oneWarning.getProportion());
        CountAndProportion twoWarning = countAndProportionService.twoWarning(statConclusions);
        table.setTwoWarningCount(twoWarning.getCount());
        table.setTwoWarningProportion(twoWarning.getProportion());
        CountAndProportion threeWarning = countAndProportionService.threeWarning(statConclusions);
        table.setThreeWarningCount(threeWarning.getCount());
        table.setThreeWarningProportion(threeWarning.getProportion());
        CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(statConclusions);
        table.setRecommendDoctorCount(recommendDoctor.getCount());
        table.setRecommendDoctorProportion(recommendDoctor.getProportion());
        CountAndProportion warning = countAndProportionService.warning(statConclusions);
        table.setWarningCount(warning.getCount());
        table.setWarningProportion(warning.getProportion());
        tables.add(table);
        return tables;
    }

    public List<GradeLowVision.Table> gradeLowVisionTable(List<StatConclusion> statConclusions) {
        List<GradeLowVision.Table> tables = new ArrayList<>();
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        List<String> kindergartenCodeList = GradeCodeEnum.kindergartenSchoolCode();
        kindergartenCodeList.forEach(code -> {
            List<StatConclusion> list = collect.getOrDefault(code, null);
            if (Objects.isNull(list)) {
                return;
            }
            Map<String, List<StatConclusion>> classList = list.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolClassName));
            classList.forEach((k, v) -> {
                GradeLowVision.Table table = new GradeLowVision.Table();
                table.setName(GradeCodeEnum.getName(code));
                table.setClassName(k);
                table.setValidCount(v.size());
                CountAndProportion male = countAndProportionService.male(v);
                table.setMCount(male.getCount());
                table.setMProportion(male.getProportion());
                CountAndProportion female = countAndProportionService.female(v);
                table.setFCount(female.getCount());
                table.setFProportion(female.getProportion());
                table.setLowVisionProportion(countAndProportionService.lowVision(v).getProportion());
                tables.add(table);
            });
        });
        GradeLowVision.Table table = new GradeLowVision.Table();
        table.setName(CommonReportService.TOTAL_NAME);
        table.setClassName(CommonReportService.TOTAL_NAME);
        table.setValidCount(statConclusions.size());
        CountAndProportion male = countAndProportionService.male(statConclusions);
        table.setMCount(male.getCount());
        table.setMProportion(male.getProportion());
        CountAndProportion female = countAndProportionService.female(statConclusions);
        table.setFCount(female.getCount());
        table.setFProportion(female.getProportion());
        table.setLowVisionProportion(countAndProportionService.lowVision(statConclusions).getProportion());
        tables.add(table);
        return tables;
    }

}


package com.wupol.myopia.business.api.management.service.report;

import com.wupol.framework.domain.ThreeTuple;
import com.wupol.myopia.base.util.AgeGeneration;
import com.wupol.myopia.base.util.AgeGenerationEnum;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.KindergartenScreeningInfoTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.PrimaryScreeningInfoTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary.WarningTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.AreaHistoryLowVisionTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.GenderSexLowVisionTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.SchoolAgeGenderTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CommonWarningTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.CountAndProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.LowVisionLevelTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.MyopiaTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AgeWearingTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.CommonLowVisionTable;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    public List<CommonLowVisionTable> gradeSchoolAgeLowVisionTable(List<StatConclusion> statConclusions, Long totalCount) {
        List<CommonLowVisionTable> tables = new ArrayList<>();
        boolean haveSenior = commonReportService.isHaveSenior(statConclusions);
        // 学龄段
        SchoolAge.getList().forEach(k -> {
            List<StatConclusion> v = statConclusions.stream().filter(s -> Objects.equals(s.getSchoolAge(), k)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(v)) {
                return;
            }
            v.sort(Comparator.comparing((StatConclusion statConclusion) -> Integer.valueOf(GradeCodeEnum.getByCode(statConclusion.getSchoolGradeCode()).getCode())));
            Map<String, List<StatConclusion>> gradeMap = v.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode, LinkedHashMap::new, Collectors.toList()));
            // 年级
            for (Map.Entry<String, List<StatConclusion>> entry : gradeMap.entrySet()) {
                CommonLowVisionTable table = new CommonLowVisionTable();
                List<StatConclusion> value = entry.getValue();
                table.setName(GradeCodeEnum.getDesc(entry.getKey()));
                extracted(tables, table, value, totalCount);
            }
            CommonLowVisionTable schoolAgeTotal = new CommonLowVisionTable();
            schoolAgeTotal.setName(SchoolAge.get(k).desc);
            extracted(tables, schoolAgeTotal, v, totalCount);

            // 如果有普高和职高，则添加多一条高中数据
            if (haveSenior && SchoolAge.VOCATIONAL_HIGH.code.equals(k)) {
                List<StatConclusion> seniorList = commonReportService.getSeniorList(statConclusions);
                CommonLowVisionTable senior = new CommonLowVisionTable();
                senior.setName(CommonReportService.SENIOR_NAME);
                extracted(tables, senior, seniorList, totalCount);
            }
        });
        CommonLowVisionTable total = new CommonLowVisionTable();
        total.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, total, statConclusions, totalCount);
        return tables;
    }

    public List<CommonLowVisionTable> schoolAgeLowVisionTables(List<StatConclusion> statConclusions, Long totalCount) {
        List<CommonLowVisionTable> tables = new ArrayList<>();
        TwoTuple<Integer, Integer> statAgeRange = commonReportService.getStatAgeRange(statConclusions);
        List<AgeGeneration> ageList = AgeGenerationEnum.getAllList(statAgeRange.getFirst(), statAgeRange.getSecond());
        for (AgeGeneration age : ageList) {
            CommonLowVisionTable table = new CommonLowVisionTable();
            table.setName(age.getDesc());
            List<StatConclusion> value = statConclusions.stream()
                    .filter(s -> s.getAge() >= age.getLeftAge() && s.getAge() < age.getRightAge()).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(value)) {
                continue;
            }
            extracted(tables, table, value, totalCount);
        }
        CommonLowVisionTable total = new CommonLowVisionTable();
        total.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, total, statConclusions, totalCount);
        return tables;
    }

    public List<RefractiveTable> genderRefractiveTable(List<StatConclusion> statConclusions, Long totalCount) {
        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<RefractiveTable> tables = new ArrayList<>();
        List<Integer> genderList = GenderEnum.genderTypeList();
        genderList.forEach(gender -> {
            RefractiveTable table = new RefractiveTable();
            table.setName(GenderEnum.getCnName(gender));
            List<StatConclusion> v = genderMap.getOrDefault(gender, new ArrayList<>());
            extracted(tables, table, v, totalCount);
        });
        RefractiveTable total = new RefractiveTable();
        total.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, total, statConclusions, totalCount);
        return tables;
    }

    private void extracted(List<RefractiveTable> tables, RefractiveTable table, List<StatConclusion> v, Long total) {
        table.setValidCount((long) v.size());

        generateRefractiveInfo(table, v, total);
        tables.add(table);
    }

    /**
     * 不同学龄段屈光筛查情况
     */
    public List<RefractiveTable> schoolAgeRefractiveTable(List<StatConclusion> statConclusions, Long total) {
        Map<String, List<StatConclusion>> gradeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode, LinkedHashMap::new, Collectors.toList()));
        List<RefractiveTable> tables = new ArrayList<>();
        gradeMap.forEach((k, v) -> {
            RefractiveTable table = new RefractiveTable();
            table.setName(GradeCodeEnum.getDesc(k));
            extracted(tables, table, v, total);
        });
        RefractiveTable table = new RefractiveTable();
        table.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, table, statConclusions, total);

        return tables;
    }

    public List<RefractiveTable> ageRefractiveTable(List<StatConclusion> statConclusions, Long total) {
        List<RefractiveTable> tables = new ArrayList<>();
        if (CollectionUtils.isEmpty(statConclusions)) {
            return new ArrayList<>();
        }
        TwoTuple<Integer, Integer> statAgeRange = commonReportService.getStatAgeRange(statConclusions);
        List<AgeGeneration> ageList = AgeGenerationEnum.getKList(statAgeRange.getFirst(), statAgeRange.getSecond());
        ageList.forEach(age -> {
            RefractiveTable table = new RefractiveTable();
            List<StatConclusion> list = statConclusions.stream()
                    .filter(s -> s.getAge() >= age.getLeftAge() && s.getAge() < age.getRightAge()).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list)) {
                return;
            }

            table.setName(age.getDesc());
            extracted(tables, table, list, total);
        });
        RefractiveTable table = new RefractiveTable();
        table.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, table, statConclusions, total);
        return tables;
    }

    public List<RefractiveTable> getRefractiveTables(List<ThreeTuple<Integer, String, List<StatConclusion>>> tuples, Integer planOrNoticeId) {
        List<RefractiveTable> tables = new ArrayList<>();
        tuples.forEach(s -> {
            RefractiveTable table = new RefractiveTable();
            List<StatConclusion> list = s.getThird();
            long total = list.size();
            table.setName(s.getSecond());
            table.setValidCount(total);
            table.setIsSameReport(s.getFirst().equals(planOrNoticeId));
            generateRefractiveTable(tables, table, list, total);
        });
        return tables;
    }

    private void generateRefractiveTable(List<RefractiveTable> tables, RefractiveTable table, List<StatConclusion> list, long total) {
        generateRefractiveInfo(table, list, total);

        CountAndProportion lowVision = countAndProportionService.lowVision(list, total);
        table.setLowVisionStudentCount(lowVision.getCount());
        table.setLowVisionProportion(lowVision.getProportion());
        tables.add(table);
    }

    private void generateRefractiveInfo(RefractiveTable table, List<StatConclusion> list, long total) {
        CountAndProportion insufficient = countAndProportionService.insufficient(list, total);
        table.setInsufficientStudentCount(insufficient.getCount());
        table.setInsufficientProportion(insufficient.getProportion());

        CountAndProportion refractiveError = countAndProportionService.refractiveError(list, total);
        table.setRefractiveErrorStudentCount(refractiveError.getCount());
        table.setRefractiveErrorProportion(refractiveError.getProportion());

        CountAndProportion anisometropia = countAndProportionService.anisometropia(list, total);
        table.setAnisometropiaStudentCount(anisometropia.getCount());
        table.setAnisometropiaProportion(anisometropia.getProportion());

        CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(list, total);
        table.setRecommendDoctorCount(recommendDoctor.getCount());
        table.setRecommendDoctorProportion(recommendDoctor.getProportion());
    }

    public List<AstigmatismTable> genderPrimaryRefractiveTable(List<StatConclusion> statConclusions, Long total) {
        List<AstigmatismTable> tables = new ArrayList<>();
        List<Integer> genderList = GenderEnum.genderTypeList();
        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        genderList.forEach(gender -> {
            List<StatConclusion> v = genderMap.getOrDefault(gender, new ArrayList<>());
            AstigmatismTable table = new AstigmatismTable();
            table.setName(GenderEnum.getCnName(gender));
            extracted(tables, v, table, total);
        });
        AstigmatismTable table = new AstigmatismTable();
        table.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, statConclusions, table, total);
        return tables;
    }

    private void extracted(List<AstigmatismTable> tables, List<StatConclusion> v, AstigmatismTable table, Long total) {
        table.setValidCount((long) v.size());

        CountAndProportion myopia = countAndProportionService.myopia(v, total);
        table.setMyopiaCount(myopia.getCount());
        table.setMyopiaProportion(myopia.getProportion());

        CountAndProportion astigmatism = countAndProportionService.astigmatism(v, total);
        table.setAstigmatismCount(astigmatism.getCount());
        table.setAstigmatismProportion(astigmatism.getProportion());

        CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(v, total);
        table.setEarlyMyopiaCount(earlyMyopia.getCount());
        table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

        CountAndProportion lightMyopia = countAndProportionService.lightMyopia(v, total);
        table.setLightMyopiaCount(lightMyopia.getCount());
        table.setLightMyopiaProportion(lightMyopia.getProportion());

        CountAndProportion highMyopia = countAndProportionService.highMyopia(v, total);
        table.setHighMyopiaCount(highMyopia.getCount());
        table.setHighMyopiaProportion(highMyopia.getProportion());
        tables.add(table);
    }

    public List<AstigmatismTable> schoolPrimaryRefractiveTable(List<StatConclusion> statConclusions, Long total) {
        List<AstigmatismTable> tables = new ArrayList<>();
        boolean haveSenior = commonReportService.isHaveSenior(statConclusions);
        SchoolAge.getList().forEach(x -> {
            List<StatConclusion> y = statConclusions.stream().filter(s -> Objects.equals(s.getSchoolAge(), x)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(y)) {
                return;
            }
            y.sort(Comparator.comparing((StatConclusion statConclusion) -> Integer.valueOf(GradeCodeEnum.getByCode(statConclusion.getSchoolGradeCode()).getCode())));
            Map<String, List<StatConclusion>> gradeMap = y.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode, LinkedHashMap::new, Collectors.toList()));
            gradeMap.forEach((k, v) -> {
                AstigmatismTable table = new AstigmatismTable();
                table.setName(GradeCodeEnum.getDesc(k));
                extracted(tables, v, table, total);
            });
            AstigmatismTable table = new AstigmatismTable();
            table.setName(SchoolAge.get(x).desc);
            extracted(tables, y, table, total);
            // 如果有普高和职高，则添加多一条高中数据
            if (haveSenior && SchoolAge.VOCATIONAL_HIGH.code.equals(x)) {
                List<StatConclusion> seniorList = commonReportService.getSeniorList(statConclusions);
                AstigmatismTable seniorTable = new AstigmatismTable();
                seniorTable.setName(CommonReportService.SENIOR_NAME);
                extracted(tables, seniorList, seniorTable, total);
            }
        });
        AstigmatismTable table = new AstigmatismTable();
        table.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, statConclusions, table, total);
        return tables;
    }

    public List<AstigmatismTable> ageAstigmatismTables(List<StatConclusion> statConclusions, Long total) {
        List<AstigmatismTable> tables = new ArrayList<>();
        if (CollectionUtils.isEmpty(statConclusions)) {
            return tables;
        }
        TwoTuple<Integer, Integer> statAgeRange = commonReportService.getStatAgeRange(statConclusions);
        List<AgeGeneration> ageList = AgeGenerationEnum.getPList(statAgeRange.getFirst(), statAgeRange.getSecond());
        ageList.forEach(age -> {
            AstigmatismTable table = new AstigmatismTable();
            List<StatConclusion> list = statConclusions.stream()
                    .filter(s -> s.getAge() >= age.getLeftAge() && s.getAge() < age.getRightAge()).collect(Collectors.toList());
            table.setName(age.getDesc());
            extracted(tables, table, list, total);
        });
        AstigmatismTable table = new AstigmatismTable();
        table.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, table, statConclusions, total);
        return tables;
    }

    private void extracted(List<AstigmatismTable> tables, AstigmatismTable table, List<StatConclusion> list, Long total) {
        table.setValidCount((long) list.size());
        generateMyopiaInfo(tables, table, list, total);
    }

    public List<AstigmatismTable> pHistoryAstigmatismTable(List<ThreeTuple<Integer, String, List<StatConclusion>>> tuples, Integer noticeId) {
        List<AstigmatismTable> tables = new ArrayList<>();
        tuples.forEach(s -> {
            AstigmatismTable table = new AstigmatismTable();
            table.setName(s.getSecond());
            List<StatConclusion> list = s.getThird();
            long total = list.size();
            table.setValidCount(total);
            table.setIsSameReport(s.getFirst().equals(noticeId));
            generateMyopiaInfo(tables, table, list, total);
        });
        return tables;
    }

    private void generateMyopiaInfo(List<AstigmatismTable> tables, AstigmatismTable table, List<StatConclusion> list, Long total) {
        CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(list, total);
        table.setEarlyMyopiaCount(earlyMyopia.getCount());
        table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

        CountAndProportion myopia = countAndProportionService.myopia(list, total);
        table.setMyopiaCount(myopia.getCount());
        table.setMyopiaProportion(myopia.getProportion());

        CountAndProportion astigmatism = countAndProportionService.astigmatism(list, total);
        table.setAstigmatismCount(astigmatism.getCount());
        table.setAstigmatismProportion(astigmatism.getProportion());

        CountAndProportion lightMyopia = countAndProportionService.lightMyopia(list, total);
        table.setLightMyopiaCount(lightMyopia.getCount());
        table.setLightMyopiaProportion(lightMyopia.getProportion());

        CountAndProportion highMyopia = countAndProportionService.highMyopia(list, total);
        table.setHighMyopiaCount(highMyopia.getCount());
        table.setHighMyopiaProportion(highMyopia.getProportion());
        tables.add(table);
    }

    public List<AstigmatismTable> gradePrimaryRefractiveTable(List<StatConclusion> statConclusions, Long total) {
        List<AstigmatismTable> tables = new ArrayList<>();
        Map<String, List<StatConclusion>> gradeMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        GradeCodeEnum.primaryAbove().forEach(k -> {
            List<StatConclusion> v = gradeMap.get(k);
            if (CollectionUtils.isEmpty(v)) {
                return;
            }
            AstigmatismTable table = new AstigmatismTable();
            table.setName(GradeCodeEnum.getDesc(k));
            extracted(tables, v, table, total);
        });
        AstigmatismTable table = new AstigmatismTable();
        table.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, statConclusions, table, total);
        return tables;
    }

    public List<AgeWearingTable> genderWearingTable(List<StatConclusion> statConclusions, Long total) {
        List<AgeWearingTable> tables = new ArrayList<>();
        List<Integer> genderList = GenderEnum.genderTypeList();
        Map<Integer, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        genderList.forEach(gender -> {
            AgeWearingTable table = new AgeWearingTable();
            List<StatConclusion> v = collect.getOrDefault(gender, new ArrayList<>());
            table.setName(GenderEnum.getCnName(gender));
            extracted(tables, table, v, total);
        });
        AgeWearingTable table = new AgeWearingTable();
        table.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, table, statConclusions, total);
        return tables;
    }

    private void extracted(List<AgeWearingTable> tables, AgeWearingTable table, List<StatConclusion> v, Long total) {
        table.setValidCount((long) v.size());

        CountAndProportion notWearing = countAndProportionService.notWearing(v, total);
        table.setNotWearing(notWearing.getCount());
        table.setNotWearingProportion(notWearing.getProportion());

        CountAndProportion glasses = countAndProportionService.glasses(v, total);
        table.setGlasses(glasses.getCount());
        table.setGlassesProportion(glasses.getProportion());

        CountAndProportion contact = countAndProportionService.contact(v, total);
        table.setWearingContact(contact.getCount());
        table.setWearingContactProportion(contact.getProportion());

        CountAndProportion night = countAndProportionService.night(v, total);
        table.setNightWearing(night.getCount());
        table.setNightWearingProportion(night.getProportion());

        CountAndProportion enough = countAndProportionService.enough(v, total);
        table.setEnough(enough.getCount());
        table.setEnoughProportion(enough.getProportion());

        CountAndProportion uncorrected = countAndProportionService.uncorrected(v, total);
        table.setUncorrected(uncorrected.getCount());
        table.setUncorrectedProportion(uncorrected.getProportion());

        CountAndProportion under = countAndProportionService.under(v, total);
        table.setUnder(under.getCount());
        table.setUnderProportion(under.getProportion());
        tables.add(table);
    }

    public List<AgeWearingTable> gradeWearingTable(List<StatConclusion> statConclusions, Long total) {
        List<AgeWearingTable> tables = new ArrayList<>();
        boolean haveSenior = commonReportService.isHaveSenior(statConclusions);
        SchoolAge.getList().forEach(x -> {
            List<StatConclusion> y = statConclusions.stream().filter(s -> Objects.equals(s.getSchoolAge(), x)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(y)) {
                return;
            }
            y.sort(Comparator.comparing((StatConclusion statConclusion) -> Integer.valueOf(GradeCodeEnum.getByCode(statConclusion.getSchoolGradeCode()).getCode())));
            Map<String, List<StatConclusion>> gradeMap = y.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode, LinkedHashMap::new, Collectors.toList()));
            gradeMap.forEach((k, v) -> {
                AgeWearingTable table = new AgeWearingTable();
                table.setName(GradeCodeEnum.getDesc(k));
                extracted(tables, table, v, total);
            });
            AgeWearingTable table = new AgeWearingTable();
            table.setName(SchoolAge.get(x).desc);
            extracted(tables, table, y, total);
            // 如果有普高和职高，则添加多一条高中数据
            if (haveSenior && SchoolAge.VOCATIONAL_HIGH.code.equals(x)) {
                List<StatConclusion> seniorList = commonReportService.getSeniorList(statConclusions);
                AgeWearingTable ageWearingTable = new AgeWearingTable();
                ageWearingTable.setName(CommonReportService.SENIOR_NAME);
                extracted(tables, ageWearingTable, seniorList, total);
            }
        });

        AgeWearingTable table = new AgeWearingTable();
        table.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, table, statConclusions, total);

        return tables;
    }

    public List<AgeWearingTable> agePrimaryWearingTable(List<StatConclusion> statConclusions, Long total) {
        List<AgeWearingTable> tables = new ArrayList<>();
        TwoTuple<Integer, Integer> statAgeRange = commonReportService.getStatAgeRange(statConclusions);
        List<AgeGeneration> ageList = AgeGenerationEnum.getPList(statAgeRange.getFirst(), statAgeRange.getSecond());
        ageList.forEach(age -> {
            AgeWearingTable table = new AgeWearingTable();
            table.setName(age.getDesc());
            List<StatConclusion> list = statConclusions.stream()
                    .filter(s -> s.getAge() >= age.getLeftAge() && s.getAge() < age.getRightAge()).collect(Collectors.toList());
            extracted(tables, table, list, total);
        });
        AgeWearingTable table = new AgeWearingTable();
        table.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, table, statConclusions, total);
        return tables;
    }

    public List<AgeWearingTable> gradePrimaryWearingTable(List<StatConclusion> statConclusions, Long total) {
        List<AgeWearingTable> tables = new ArrayList<>();
        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        GradeCodeEnum.primaryAbove().forEach(k -> {
            List<StatConclusion> v = collect.get(k);
            if (CollectionUtils.isEmpty(v)) {
                return;
            }
            AgeWearingTable table = new AgeWearingTable();
            table.setName(GradeCodeEnum.getDesc(k));
            extracted(tables, table, v, total);
        });
        AgeWearingTable table = new AgeWearingTable();
        table.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, table, statConclusions, total);
        return tables;
    }

    public List<WarningTable> warningTable(List<StatConclusion> statConclusions, Long total) {
        List<WarningTable> tables = new ArrayList<>();
        boolean haveSenior = commonReportService.isHaveSenior(statConclusions);
        SchoolAge.getList().forEach(x -> {
            List<StatConclusion> y = statConclusions.stream().filter(s -> Objects.equals(s.getSchoolAge(), x)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(y)) {
                return;
            }
            y.sort(Comparator.comparing((StatConclusion statConclusion) -> Integer.valueOf(GradeCodeEnum.getByCode(statConclusion.getSchoolGradeCode()).getCode())));
            Map<String, List<StatConclusion>> gradeMap = y.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode, LinkedHashMap::new, Collectors.toList()));
            gradeMap.forEach((k, v) -> {
                WarningTable table = new WarningTable();
                table.setName(GradeCodeEnum.getDesc(k));
                generateWarningDate(tables, v, table, total);
            });
            WarningTable table = new WarningTable();
            table.setName(SchoolAge.get(x).desc);
            generateWarningDate(tables, y, table, total);
            // 如果有普高和职高，则添加多一条高中数据
            if (haveSenior && SchoolAge.VOCATIONAL_HIGH.code.equals(x)) {

                List<StatConclusion> seniorList = commonReportService.getSeniorList(statConclusions);

                WarningTable seniorTable = new WarningTable();
                seniorTable.setName(CommonReportService.SENIOR_NAME);
                generateWarningDate(tables, seniorList, seniorTable, total);
            }
        });
        WarningTable table = new WarningTable();
        table.setName(CommonReportService.TOTAL_NAME);
        generateWarningDate(tables, statConclusions, table, total);
        return tables;
    }

    private void generateWarningDate(List<WarningTable> tables, List<StatConclusion> v, WarningTable table, Long total) {
        table.setValidCount(v.size());
        generateWarningDateInfo(table, v, total, false);
        tables.add(table);
    }

    public  <T extends CommonWarningTable> void generateWarningDateInfo(T table, List<StatConclusion> v, Long total, boolean isK) {
        CountAndProportion zeroWarning;
        if (isK) {
            zeroWarning = countAndProportionService.zeroAndSPWarning(v, total);
        } else {
            zeroWarning = countAndProportionService.zeroWarning(v, total);
        }

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
    }

    public List<KindergartenScreeningInfoTable> kindergartenScreeningInfoTables(List<StatConclusion> statConclusions, Long total) {
        List<KindergartenScreeningInfoTable> tables = new ArrayList<>();
        if (CollectionUtils.isEmpty(statConclusions)) {
            return tables;
        }
        List<Integer> schoolIds = statConclusions.stream().map(StatConclusion::getSchoolId).collect(Collectors.toList());
        Map<Integer, String> schoolMap = schoolService.getByIds(schoolIds).stream().collect(Collectors.toMap(School::getId, School::getName));

        Map<Integer, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
        collect.forEach((k, v) -> {
            KindergartenScreeningInfoTable table = new KindergartenScreeningInfoTable();
            table.setName(schoolMap.get(k));
            table.setValidCount(v.size());

            CountAndProportion lowVision = countAndProportionService.lowVision(v, total);
            table.setLowVisionCount(lowVision.getCount());
            table.setLowVisionProportion(lowVision.getProportion());

            table.setAvgVision(commonReportService.getAvgVision(v));

            CountAndProportion insufficient = countAndProportionService.insufficient(v, total);
            table.setInsufficientCount(insufficient.getCount());
            table.setInsufficientProportion(insufficient.getProportion());

            CountAndProportion refractiveError = countAndProportionService.refractiveError(v, total);
            table.setRefractiveErrorCount(refractiveError.getCount());
            table.setRefractiveErrorProportion(refractiveError.getProportion());

            CountAndProportion anisometropia = countAndProportionService.anisometropia(v, total);
            table.setAnisometropiaCount(anisometropia.getCount());
            table.setAnisometropiaProportion(anisometropia.getProportion());

            CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(v, total);
            table.setRecommendDoctorCount(recommendDoctor.getCount());
            table.setRecommendDoctorProportion(recommendDoctor.getProportion());
            tables.add(table);
        });
        return tables.stream().sorted(Comparator.comparing(KindergartenScreeningInfoTable::getLowVisionProportion).reversed()).collect(Collectors.toList());
    }

    public List<PrimaryScreeningInfoTable> schoolScreeningInfoTables(List<StatConclusion> statConclusions) {
        List<PrimaryScreeningInfoTable> tables = new ArrayList<>();
        long total = statConclusions.size();
        List<Integer> schoolIds = statConclusions.stream().map(StatConclusion::getSchoolId).collect(Collectors.toList());
        Map<Integer, String> schoolMap = schoolService.getByIds(schoolIds).stream().collect(Collectors.toMap(School::getId, School::getName));

        Map<Integer, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
        collect.forEach((k, v) -> {
            PrimaryScreeningInfoTable table = new PrimaryScreeningInfoTable();
            table.setName(schoolMap.get(k));
            generateScreeningInfoTable(total, tables, v, table);
        });
        return tables;
    }

    public List<PrimaryScreeningInfoTable> primaryScreeningInfoTables(List<StatConclusion> statConclusions, Long total) {
        List<PrimaryScreeningInfoTable> tables = new ArrayList<>();

        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolClassName));
        collect.forEach((k, v) -> {
            PrimaryScreeningInfoTable table = new PrimaryScreeningInfoTable();
            table.setName(k);
            generateScreeningInfoTable(total, tables, v, table);
        });
        return tables;
    }

    private void generateScreeningInfoTable(Long total, List<PrimaryScreeningInfoTable> tables, List<StatConclusion> v, PrimaryScreeningInfoTable table) {
        table.setValidCount(v.size());

        CountAndProportion lowVision = countAndProportionService.lowVision(v, total);
        table.setLowVisionCount(lowVision.getCount());
        table.setLowVisionProportion(lowVision.getProportion());

        table.setAvgVision(commonReportService.getAvgVision(v));

        CountAndProportion myopia = countAndProportionService.myopia(v, total);
        table.setMyopiaCount(myopia.getCount());
        table.setMyopiaProportion(myopia.getProportion());

        CountAndProportion earlyMyopia = countAndProportionService.earlyMyopia(v, total);
        table.setEarlyMyopiaCount(earlyMyopia.getCount());
        table.setEarlyMyopiaProportion(earlyMyopia.getProportion());

        CountAndProportion lightMyopia = countAndProportionService.lightMyopia(v, total);
        table.setLightMyopiaCount(lightMyopia.getCount());
        table.setLightMyopiaProportion(lightMyopia.getProportion());

        CountAndProportion highMyopia = countAndProportionService.highMyopia(v, total);
        table.setHighMyopiaCount(highMyopia.getCount());
        table.setHighMyopiaProportion(highMyopia.getProportion());

        CountAndProportion recommendDoctor = countAndProportionService.getRecommendDoctor(v, total);
        table.setRecommendDoctorCount(recommendDoctor.getCount());
        table.setRecommendDoctorProportion(recommendDoctor.getProportion());

        CountAndProportion underAndUncorrected = countAndProportionService.underAndUncorrected(v);
        table.setOweCount(underAndUncorrected.getCount());
        table.setOweProportion(underAndUncorrected.getProportion());
        tables.add(table);
    }


    public List<AreaHistoryLowVisionTable> historySchoolAgeLowVisionTables(List<ThreeTuple<Integer, String, List<StatConclusion>>> tuples,
                                                                           Integer noticeId) {
        List<AreaHistoryLowVisionTable> tables = new ArrayList<>();

        tuples.forEach(tuple -> {
            AreaHistoryLowVisionTable table = new AreaHistoryLowVisionTable();
            table.setName(tuple.getSecond());

            List<StatConclusion> list = tuple.getThird();

            long total = list.size();
            table.setValidCount(total);

            List<StatConclusion> kList = commonReportService.getKList(list);
            List<StatConclusion> pList = commonReportService.getPList(list);

            CountAndProportion kLowVision = countAndProportionService.lowVision(kList, total);
            table.setKLowVisionCount(kLowVision.getCount());
            table.setKLowVisionProportion(kLowVision.getProportion());

            generateLowVisionTable(table, pList, total);
            table.setIsSameReport(tuple.getFirst().equals(noticeId));
            tables.add(table);
        });
        return tables;
    }

    /**
     * 学龄段性别统计表格
     */
    public List<SchoolAgeGenderTable> areaOutlineTable(List<StatConclusion> statConclusions, List<ScreeningPlanSchoolStudent> planStudents) {
        List<SchoolAgeGenderTable> tables = new ArrayList<>();
        boolean haveSenior = commonReportService.isHaveSenior(statConclusions);
        List<Integer> schoolAges = SchoolAge.sortList(statConclusions.stream().map(StatConclusion::getSchoolAge).distinct().collect(Collectors.toList()));

        for (Integer schoolAge : schoolAges) {
            SchoolAgeGenderTable schoolAgeGenderTable = new SchoolAgeGenderTable();
            schoolAgeGenderTable.setName(SchoolAge.get(schoolAge).desc);
            List<StatConclusion> list = statConclusions.stream().filter(s -> Objects.equals(s.getSchoolAge(), schoolAge)).collect(Collectors.toList());
            List<ScreeningPlanSchoolStudent> students = planStudents.stream().filter(s -> Objects.equals(s.getGradeType(), schoolAge)).collect(Collectors.toList());

            extracted(tables, schoolAgeGenderTable, list, students);

            if (haveSenior && SchoolAge.VOCATIONAL_HIGH.code.equals(schoolAge)) {
                SchoolAgeGenderTable seniorTable = new SchoolAgeGenderTable();
                seniorTable.setName(CommonReportService.SENIOR_NAME);
                List<StatConclusion> seniorList = commonReportService.getSeniorList(statConclusions);
                extracted(tables, seniorTable, seniorList, commonReportService.getPlanStudentSeniorList(planStudents));
            }
        }
        SchoolAgeGenderTable total = new SchoolAgeGenderTable();
        total.setName(CommonReportService.TOTAL_NAME);
        List<StatConclusion> mList = statConclusions.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.MALE.type)).collect(Collectors.toList());
        List<StatConclusion> fList = statConclusions.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.FEMALE.type)).collect(Collectors.toList());

        total.setMCount(planStudents.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.MALE.type)).count());
        total.setMValidCount(mList.stream().filter(StatConclusion::getIsValid).count());

        total.setFCount(planStudents.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.FEMALE.type)).count());
        total.setFValidCount(fList.stream().filter(StatConclusion::getIsValid).count());

        total.setTotalCount((long) planStudents.size());
        total.setTotalValidCount(statConclusions.stream().filter(StatConclusion::getIsValid).count());
        total.setTotalValidProportion(BigDecimalUtil.divide(total.getTotalValidCount(), total.getTotalCount()));
        tables.add(total);
        return tables;
    }

    private void extracted(List<SchoolAgeGenderTable> tables, SchoolAgeGenderTable table, List<StatConclusion> list, List<ScreeningPlanSchoolStudent> students) {
        table.setMCount(students.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.MALE.type)).count());
        table.setMValidCount(list.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.MALE.type)).filter(StatConclusion::getIsValid).count());

        table.setFCount(students.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.FEMALE.type)).count());
        table.setFValidCount(list.stream().filter(s -> Objects.equals(s.getGender(), GenderEnum.FEMALE.type)).filter(StatConclusion::getIsValid).count());

        table.setTotalCount((long) students.size());
        table.setTotalValidCount(list.stream().filter(StatConclusion::getIsValid).count());
        table.setTotalValidProportion(BigDecimalUtil.divide(table.getTotalValidCount(), table.getTotalCount()));
        tables.add(table);
    }

    public List<GenderSexLowVisionTable> genderSexLowVisionTable(List<StatConclusion> statConclusions, Long totalCount) {
        // 表格
        List<GenderSexLowVisionTable> tables = new ArrayList<>();
        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<Integer> genderList = GenderEnum.genderTypeList();
        genderList.forEach(gender -> {
            GenderSexLowVisionTable table = new GenderSexLowVisionTable();
            List<StatConclusion> v = genderMap.getOrDefault(gender, new ArrayList<>());
            table.setName(GenderEnum.getCnName(gender));
            table.setValidCount(commonReportService.getValidList(v).size());
            extracted(tables, table, v, totalCount);
        });
        GenderSexLowVisionTable total = new GenderSexLowVisionTable();
        total.setName(CommonReportService.TOTAL_NAME);
        total.setValidCount(statConclusions.size());
        extracted(tables, total, statConclusions, totalCount);
        return tables;
    }

    private void extracted(List<GenderSexLowVisionTable> tables, GenderSexLowVisionTable table, List<StatConclusion> v, Long total) {
        List<StatConclusion> kList = commonReportService.getKList(v);
        List<StatConclusion> pList = commonReportService.getPList(v);

        CountAndProportion kLowVision = countAndProportionService.lowVision(kList, total);
        table.setKAvgVision(commonReportService.getAvgVision(kList));
        table.setKCount(kLowVision.getCount());
        table.setKProportion(kLowVision.getProportion());

        CountAndProportion pLowVision = countAndProportionService.lowVision(pList, total);
        table.setPCount(pLowVision.getCount());
        table.setPProportion(pLowVision.getProportion());
        table.setPAvgVision(commonReportService.getAvgVision(pList));

        tables.add(table);
    }

    public List<MyopiaTable> historyMyopiaTables(List<ThreeTuple<Integer, String, List<StatConclusion>>> tuples,
                                                 Integer planId) {
        List<MyopiaTable> tables = new ArrayList<>();

        tuples.forEach(s -> {
            MyopiaTable table = new MyopiaTable();

            List<StatConclusion> list = s.getThird();
            table.setName(s.getSecond());
            long total = list.size();
            table.setValidCount(total);

            table.setIsSameReport(s.getFirst().equals(planId));

            CountAndProportion lowVision = countAndProportionService.lowVision(list, total);
            table.setLowVision(lowVision.getCount());
            table.setLowVisionProportion(lowVision.getProportion());

            CountAndProportion myopia = countAndProportionService.myopia(list, total);
            table.setMyopia(myopia.getCount());
            table.setMyopiaProportion(myopia.getProportion());

            CountAndProportion early = countAndProportionService.earlyMyopia(list, total);
            table.setEarly(early.getCount());
            table.setEarlyProportion(early.getProportion());

            CountAndProportion light = countAndProportionService.lightMyopia(list, total);
            table.setLight(light.getCount());
            table.setLightProportion(light.getProportion());

            CountAndProportion middle = countAndProportionService.middleMyopia(list);
            table.setMiddle(middle.getCount());
            table.setMiddleProportion(middle.getProportion());

            CountAndProportion high = countAndProportionService.highMyopia(list, total);
            table.setHigh(high.getCount());
            table.setHighProportion(high.getProportion());

            tables.add(table);
        });
        return tables;
    }

    public List<CommonLowVisionTable> lowVisionTables(List<StatConclusion> statConclusions, Long total) {
        List<CommonLowVisionTable> tables = new ArrayList<>();
        Map<Integer, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));

        List<Integer> genderList = GenderEnum.genderTypeList();
        genderList.forEach(gender -> {
            CommonLowVisionTable lowVisionTable = new CommonLowVisionTable();
            lowVisionTable.setName(GenderEnum.getCnName(gender));
            List<StatConclusion> v = collect.getOrDefault(gender, new ArrayList<>());
            extracted(tables, lowVisionTable, v, total);
        });

        CommonLowVisionTable lowVisionTable = new CommonLowVisionTable();
        lowVisionTable.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, lowVisionTable, statConclusions, total);

        return tables;
    }

    private void extracted(List<CommonLowVisionTable> tables, CommonLowVisionTable lowVisionTable, List<StatConclusion> v, Long total) {
        lowVisionTable.setValidCount((long) v.size());
        generateLowVisionTable(lowVisionTable, v, total);
        lowVisionTable.setAvgVision(commonReportService.getAvgVision(v));
        tables.add(lowVisionTable);
    }

    public <T extends LowVisionLevelTable> void generateLowVisionTable(T tables, List<StatConclusion> v, Long total) {
        CountAndProportion lowVision = countAndProportionService.lowVision(v, total);
        tables.setLowVisionCount(lowVision.getCount());
        tables.setLowVisionProportion(lowVision.getProportion());

        CountAndProportion light = countAndProportionService.lightLowVision(v, total);
        tables.setLightLowVisionCount(light.getCount());
        tables.setLightLowVisionProportion(light.getProportion());

        CountAndProportion middle = countAndProportionService.middleLowVision(v, total);
        tables.setMiddleLowVisionCount(middle.getCount());
        tables.setMiddleLowVisionProportion(middle.getProportion());

        CountAndProportion high = countAndProportionService.highLowVision(v, total);
        tables.setHighLowVisionCount(high.getCount());
        tables.setHighLowVisionProportion(high.getProportion());
    }

    public List<CommonLowVisionTable> gradeLowVision(List<StatConclusion> statConclusions, Long total) {
        List<CommonLowVisionTable> tables = new ArrayList<>();

        Map<String, List<StatConclusion>> collect = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        GradeCodeEnum.primaryAbove().forEach(k -> {
            CommonLowVisionTable lowVisionTable = new CommonLowVisionTable();
            lowVisionTable.setName(GradeCodeEnum.getDesc(k));
            List<StatConclusion> v = collect.get(k);
            if (CollectionUtils.isEmpty(v)) {
                return;
            }
            extracted(tables, lowVisionTable, v, total);
        });

        CommonLowVisionTable lowVisionTable = new CommonLowVisionTable();
        lowVisionTable.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, lowVisionTable, statConclusions, total);

        return tables;
    }

    public List<CommonLowVisionTable> ageLowTable(List<StatConclusion> statConclusions, Long total) {
        List<CommonLowVisionTable> tables = new ArrayList<>();
        if (CollectionUtils.isEmpty(statConclusions)) {
            return tables;
        }

        TwoTuple<Integer, Integer> statAgeRange = commonReportService.getStatAgeRange(statConclusions);
        List<AgeGeneration> ageList = AgeGenerationEnum.getPList(statAgeRange.getFirst(), statAgeRange.getSecond());

        ageList.forEach(age -> {
            CommonLowVisionTable table = new CommonLowVisionTable();
            table.setName(age.getDesc());
            List<StatConclusion> list = statConclusions.stream()
                    .filter(s -> s.getAge() >= age.getLeftAge() && s.getAge() < age.getRightAge()).collect(Collectors.toList());
            table.setValidCount((long) list.size());

            CommonLowVisionTable lowVisionTable = new CommonLowVisionTable();
            lowVisionTable.setName(age.getDesc());
            extracted(tables, lowVisionTable, list, total);
        });

        CommonLowVisionTable lowVisionTable = new CommonLowVisionTable();
        lowVisionTable.setName(CommonReportService.TOTAL_NAME);
        extracted(tables, lowVisionTable, statConclusions, total);
        return tables;
    }
}
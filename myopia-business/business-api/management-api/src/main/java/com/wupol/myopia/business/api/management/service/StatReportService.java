package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSONPath;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException;
import com.wupol.myopia.business.api.management.domain.dto.*;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeItemsDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.util.SchoolUtil;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningResultPahtConst;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningNoticeService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Service
public class StatReportService {
    @Autowired
    private StatConclusionService statConclusionService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private ScreeningPlanService screeningPlanService;

    @Autowired
    private ScreeningNoticeService screeningNoticeService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private SchoolGradeService schoolGradeService;

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    private final static String TABLE_LABEL_TOTAL = "total";
    private final static String TABLE_LABEL_ROW_TOTAL = "rowTotal";
    private final static String TABLE_LABEL_AVERAGE_VISION = "averageVision";
    private final static String TABLE_LABEL_TOTAL_RATIO = "totalRatio";
    private final static String TABLE_LABEL_TOTAL_STAT_LIST = "totalStatList";
    private final static String TOTAL_CHINESE = "合计";

    /**
     * 构建区域及其下级所有符合当前用户范围的区域的搜索条件
     *
     * @param districtId 区域 id
     * @return
     */
    private StatConclusionQueryDTO composeDistrictQuery(Integer districtId) {
        StatConclusionQueryDTO query = new StatConclusionQueryDTO();
        query.setDistrictIds(districtService.getSpecificDistrictTreeAllDistrictIds(districtId));
        return query;
    }

    /**
     * 构建学校性别统计表
     * @param name
     * @param statConclusions
     * @return
     */
    private Map<String, Object> composeGenderStat(
            String name, List<StatConclusion> statConclusions) {
        long total = statConclusions.size();
        long maleNum = statConclusions.stream().filter(x -> GenderEnum.MALE.type.equals(x.getGender())).count();
        long femaleNum = total - maleNum;
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name", name);
        resultMap.put("male", maleNum);
        resultMap.put("female", femaleNum);
        resultMap.put(TABLE_LABEL_TOTAL, total);
        return resultMap;
    }

    /**
     * 转换为百分比并保留2位小数
     * @param num
     * @return
     */
    private Float convertToPercentage(Float num) {
        return Math.round(num * 10000) / 100f;
    }

    /**
     * 保留2位小数
     * @param num
     * @return
     */
    private Float round2Digits(double num) {
        return Math.round(num * 100) / 100f;
    }

    /**
     * 构造报告基础数量占比分类统计数据
     * @param name 分类名称
     * @param statNum 分类统计数量
     * @param totalStatNum 统计总量
     * @return
     */
    private TableBasicStatParams composeTableBasicParams(
            String name, long statNum, long totalStatNum) {
        return new TableBasicStatParams(
                name, convertToPercentage(statNum * 1f / totalStatNum), statNum, totalStatNum);
    }

    /**
     * 构基础数量占比分类统计数据
     * @param name 分类名称
     * @param statNum 分类统计数量
     * @param totalStatNum 统计总量
     * @return
     */
    private BasicStatParams composeBasicParams(String name, long statNum, long totalStatNum) {
        return new BasicStatParams(name, convertToPercentage(statNum * 1f / totalStatNum), statNum);
    }

    /**
     * 计算平均筛查视力
     * @param statConclusions
     * @return
     */
    private AverageVision calculateAverageVision(List<StatConclusion> statConclusions) {
        int size = statConclusions.size();
        double sumVisionL = statConclusions.stream().mapToDouble(StatConclusion::getVisionL).sum();
        double sumVisionR = statConclusions.stream().mapToDouble(StatConclusion::getVisionR).sum();
        float avgVisionL = round2Digits(sumVisionL / size);
        float avgVisionR = round2Digits(sumVisionR / size);
        return AverageVision.builder().averageVisionLeft(avgVisionL).averageVisionRight(avgVisionR).build();
    }

    /** 平均视力 */
    @Data
    @Builder
    public static class AverageVision {
        private float averageVisionLeft;
        private float averageVisionRight;
    }

    /**
     * 获取区域筛查报告数据
     *
     * @param srcScreeningNoticeId 政府下发的原始通知id
     * @param districtId           区域id
     * @return
     */
    public Map<String, Object> getDistrictStatData(int srcScreeningNoticeId, int districtId) {
        StatConclusionQueryDTO query = composeDistrictQuery(districtId);
        query.setSrcScreeningNoticeId(srcScreeningNoticeId);
        List<StatConclusion> statConclusions = statConclusionService.listByQuery(query);
        if (statConclusions == null || statConclusions.size() == 0) {
            return null;
        }
        District district = districtService.getById(districtId);
        String districtName = district.getName();
        int planScreeningNum = this.getPlanScreeningStudentNum(srcScreeningNoticeId, districtId);
        ScreeningNotice notice = screeningNoticeService.getById(srcScreeningNoticeId);
        Date startDate = notice.getStartTime();
        Date endDate = notice.getEndTime();
        StatBaseDTO statBase = new StatBaseDTO(statConclusions);
        List<StatConclusion> validConclusions = statBase.getValid();

        long totalFirstScreeningNum = statBase.getFirstScreen().size();
        List<Integer> schoolIds =
                statConclusions.stream().map(StatConclusion::getSchoolId).distinct().collect(Collectors.toList());
        List<School> schools = schoolService.getByIds(schoolIds);
        List<String> schoolExamples = schools.stream().map(School::getName).limit(3).collect(Collectors.toList());

        List<StatConclusion> visionCorrectionConclusions =
                statBase.getFirstScreen().stream().filter(x -> x.getVisionCorrection() != null && !VisionCorrection.NORMAL.code.equals(x.getVisionCorrection())).collect(Collectors.toList());
        // 组装性别信息
        StatGenderDTO statGender = new StatGenderDTO(validConclusions);
        // 组装各学龄段信息
        StatBusinessSchoolAgeDTO businessSchoolAge = new StatBusinessSchoolAgeDTO(statBase, true);

        List<Map<String, Object>> schoolGenderTable = this.createSchoolGenderTable(validConclusions, schools);

        Map<String, List<StatConclusion>> schoolAgeMap = businessSchoolAge.getValidSchoolAgeMap();
        Map<String, Integer> validSchoolAgeNumMap = businessSchoolAge.getValidSchoolAgeNumMap();

        List<Map<String, Object>> schoolAgeGenderTable = this.createSchoolAgeGenderTable(schoolAgeMap);

        List<Map<String, Object>> schoolAgeLowVisionLevelTable =
                this.createSchoolAgeLowVisionLevelTable(validConclusions, schoolAgeMap);

        List<Map<String, Object>> schoolAgeGenderLowVisionTable =
                this.createSchoolAgeGenderLowVisionTable(validConclusions, schoolAgeMap);

        List<Map<String, Object>> schoolAgeGenderMyopiaTable = this.createSchoolAgeGenderMyopiaTable(validConclusions
                , schoolAgeMap);

        List<Map<String, Object>> schoolGradeGenderMyopiaTable =
                this.createSchoolGradeGenderMyopiaTable(validConclusions);

        List<Map<String, Object>> schoolAgeMyopiaLevelTable = this.createSchoolAgeMyopiaLevelTable(validConclusions,
                schoolAgeMap);

        List<Map<String, Object>> schoolAgeGlassesTypeTable = this.createSchoolAgeGlassesTypeTable(validConclusions,
                schoolAgeMap);

        List<Map<String, Object>> schoolAgeGenderVisionUncorrectedTable =
                this.createSchoolAgeGenderVisionUncorrectedTable(validConclusions, schoolAgeMap);

        List<Map<String, Object>> schoolAgeGenderVisionUnderCorrectedTable =
                this.createSchoolAgeGenderVisionUnderCorrectedTable(visionCorrectionConclusions, schoolAgeMap);

        List<Map<String, Object>> schoolAgeWarningLevelTable = this.createSchoolAgeWarningLevelTable(validConclusions
                , schoolAgeMap);

        Map<String, Object> result = new HashMap<>();
        result.put("districtName", districtName);
        if (startDate != null) {
            result.put("startDate", startDate.getTime());
        }
        if (endDate != null) {
            result.put("endDate", endDate.getTime());
        }
        result.put("schoolExamples", schoolExamples);
        result.put("planScreeningNum", planScreeningNum);
        result.put("actualScreeningNum", totalFirstScreeningNum);
        result.put("validFirstScreeningNum", validConclusions.size());
        result.put("kindergartenScreeningNum", validSchoolAgeNumMap.get(SchoolAge.KINDERGARTEN.name()));
        result.put("primaryScreeningNum", validSchoolAgeNumMap.get(SchoolAge.PRIMARY.name()));
        result.put("juniorScreeningNum", validSchoolAgeNumMap.get(SchoolAge.JUNIOR.name()));
        result.put("highScreeningNum", validSchoolAgeNumMap.get(SchoolAge.HIGH.name()));
        result.put("maleNum", statGender.getMale().size());
        result.put("femaleNum", statGender.getFemale().size());
        result.put("vocationalHighScreeningNum", validSchoolAgeNumMap.get(SchoolAge.VOCATIONAL_HIGH.name()));
        result.put("schoolGenderTable", schoolGenderTable);
        result.put("schoolAgeGenderTable", schoolAgeGenderTable);
        result.put("schoolAgeLowVisionLevelDesc", composeSchoolAgeLowVisionLevelDesc(schoolAgeLowVisionLevelTable));
        result.put("schoolAgeGenderLowVisionDesc", composeSchoolAgeGenderVisionDesc("schoolAgeGenderLowVisionTable",
                schoolAgeGenderLowVisionTable));
        result.put("schoolAgeGenderMyopiaDesc", composeSchoolAgeGenderVisionDesc("schoolAgeGenderMyopiaTable",
                schoolAgeGenderMyopiaTable));
        result.put("schoolGradeGenderMyopiaTable", schoolGradeGenderMyopiaTable);
        result.put("schoolAgeMyopiaLevelDesc", composeSchoolAgeMyopiaLevelDesc("schoolAgeMyopiaLevelTable",
                schoolAgeMyopiaLevelTable));
        result.put("schoolAgeGlassesTypeDesc", composeSchoolAgeGlassesTypeDesc("schoolAgeGlassesTypeTable",
                schoolAgeGlassesTypeTable));
        result.put("schoolAgeGenderVisionUncorrectedDesc", composeSchoolAgeGenderVisionCorrectionDesc(
                "schoolAgeGenderVisionUncorrectedTable", schoolAgeGenderVisionUncorrectedTable));
        result.put("schoolAgeGenderVisionUnderCorrectedDesc", composeSchoolAgeGenderVisionCorrectionDesc(
                "schoolAgeGenderVisionUnderCorrectedTable", schoolAgeGenderVisionUnderCorrectedTable));
        result.put("schoolAgeWarningLevelDesc", composeSchoolAgeWarningLevelDesc("schoolAgeWarningLevelTable",
                schoolAgeWarningLevelTable));
        return result;
    }

    private List<Map<String, Object>> createSchoolGenderTable(List<StatConclusion> validConclusions,
                                                              List<School> schools) {
        List<Map<String, Object>> schoolGenderTable = new ArrayList<>();
        for (School school : schools) {
            int schoolId = school.getId();
            List<StatConclusion> schoolList =
                    validConclusions.stream().filter(x -> x.getSchoolId() == schoolId).collect(Collectors.toList());
            Map<String, Object> schoolGenderStat = composeGenderStat(school.getName(), schoolList);
            schoolGenderTable.add(schoolGenderStat);
        }
        return schoolGenderTable;
    }

    private List<Map<String, Object>> createSchoolAgeWarningLevelTable(List<StatConclusion> validConclusions,
                                                                       Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeWarningLevelTable = new ArrayList<>();
        for (String schoolAgeName : schoolAgeMap.keySet()) {
            schoolAgeWarningLevelTable.add(composeWarningLevelStat(schoolAgeName, schoolAgeMap.get(schoolAgeName)));
        }
        schoolAgeWarningLevelTable.add(composeWarningLevelStat(TABLE_LABEL_TOTAL, validConclusions));
        return schoolAgeWarningLevelTable;
    }

    private List<Map<String, Object>> createSchoolAgeGenderVisionUnderCorrectedTable(List<StatConclusion> visionCorrectionConclusions, Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeGenderVisionUnderCorrectedTable = new ArrayList<>();
        for (String schoolAgeName : schoolAgeMap.keySet()) {
            List<StatConclusion> schoolAgeVisionCorrectionConclusions =
                    schoolAgeMap.get(schoolAgeName).stream().filter(x -> x.getVisionCorrection() != null && !VisionCorrection.NORMAL.code.equals(x.getVisionCorrection())).collect(Collectors.toList());
            schoolAgeGenderVisionUnderCorrectedTable.add(composeGenderVisionUnderCorrectedStat(schoolAgeName,
                    schoolAgeVisionCorrectionConclusions));
        }
        schoolAgeGenderVisionUnderCorrectedTable.add(composeGenderVisionUnderCorrectedStat(TABLE_LABEL_TOTAL,
                visionCorrectionConclusions));
        return schoolAgeGenderVisionUnderCorrectedTable;
    }

    private List<Map<String, Object>> createSchoolAgeGenderVisionUncorrectedTable(List<StatConclusion> validConclusions, Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeGenderVisionUncorrectedTable = new ArrayList<>();
        for (String schoolAgeName : schoolAgeMap.keySet()) {
            List<StatConclusion> schoolAgeMyopiaConclusions =
                    schoolAgeMap.get(schoolAgeName).stream().filter(x -> x.getIsMyopia() != null && x.getIsMyopia()).collect(Collectors.toList());
            schoolAgeGenderVisionUncorrectedTable.add(composeGenderVisionUncorrectedStat(schoolAgeName,
                    schoolAgeMyopiaConclusions));
        }
        List<StatConclusion> myopiaConclusions =
                validConclusions.stream().filter(StatConclusion::getIsMyopia).collect(Collectors.toList());
        schoolAgeGenderVisionUncorrectedTable.add(composeGenderVisionUncorrectedStat(TABLE_LABEL_TOTAL,
                myopiaConclusions));
        return schoolAgeGenderVisionUncorrectedTable;
    }

    private List<Map<String, Object>> createSchoolAgeGenderTable(Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeGenderTable = new ArrayList<>();
        for (String schoolAgeName : schoolAgeMap.keySet()) {
            schoolAgeGenderTable.add(composeGenderStat(schoolAgeName, schoolAgeMap.get(schoolAgeName)));
        }
        return schoolAgeGenderTable;
    }

    private List<Map<String, Object>> createSchoolAgeLowVisionLevelTable(List<StatConclusion> validConclusions,
                                                                         Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeLowVisionLevelTable = new ArrayList<>();
        for (String schoolAgeName : schoolAgeMap.keySet()) {
            schoolAgeLowVisionLevelTable.add(composeLowVisionLevelStat(schoolAgeName, schoolAgeMap.get(schoolAgeName)));
        }
        schoolAgeLowVisionLevelTable.add(composeLowVisionLevelStat(TABLE_LABEL_TOTAL, validConclusions));
        return schoolAgeLowVisionLevelTable;
    }

    private List<Map<String, Object>> createSchoolAgeGenderLowVisionTable(List<StatConclusion> validConclusions,
                                                                          Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeGenderLowVisionTable = new ArrayList<>();
        for (String schoolAgeName : schoolAgeMap.keySet()) {
            schoolAgeGenderLowVisionTable.add(composeGenderLowVisionStat(schoolAgeName,
                    schoolAgeMap.get(schoolAgeName)));
        }
        schoolAgeGenderLowVisionTable.add(composeGenderLowVisionStat(TABLE_LABEL_TOTAL, validConclusions));
        return schoolAgeGenderLowVisionTable;
    }

    private List<Map<String, Object>> createSchoolAgeGenderMyopiaTable(List<StatConclusion> validConclusions,
                                                                       Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeGenderMyopiaTable = new ArrayList<>();
        for (String schoolAgeName : schoolAgeMap.keySet()) {
            schoolAgeGenderMyopiaTable.add(composeGenderMyopiaStat(schoolAgeName, schoolAgeMap.get(schoolAgeName)));
        }
        schoolAgeGenderMyopiaTable.add(composeGenderMyopiaStat(TABLE_LABEL_TOTAL, validConclusions));
        return schoolAgeGenderMyopiaTable;
    }

    private List<Map<String, Object>> createSchoolAgeGlassesTypeTable(List<StatConclusion> validConclusions,
                                                                      Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeGlassesTypeTable = new ArrayList<>();
        for (String schoolAgeName : schoolAgeMap.keySet()) {
            schoolAgeGlassesTypeTable.add(composeGlassesTypeStat(schoolAgeName, schoolAgeMap.get(schoolAgeName)));
        }
        schoolAgeGlassesTypeTable.add(composeGlassesTypeStat(TABLE_LABEL_TOTAL, validConclusions));
        return schoolAgeGlassesTypeTable;
    }

    private List<Map<String, Object>> createSchoolAgeMyopiaLevelTable(List<StatConclusion> validConclusions,
                                                                      Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeMyopiaLevelTable = new ArrayList<>();
        for (String schoolAgeName : schoolAgeMap.keySet()) {
            schoolAgeMyopiaLevelTable.add(composeMyopiaLevelStat(schoolAgeName, schoolAgeMap.get(schoolAgeName)));
        }
        schoolAgeMyopiaLevelTable.add(composeMyopiaLevelStat(TABLE_LABEL_TOTAL, validConclusions));
        return schoolAgeMyopiaLevelTable;
    }

    private List<Map<String, Object>> createSchoolGradeGenderMyopiaTable(List<StatConclusion> validConclusions) {
        List<Map<String, Object>> schoolGradeGenderMyopiaTable = new ArrayList<>();
        for (GradeCodeEnum gradeCode : GradeCodeEnum.values()) {
            schoolGradeGenderMyopiaTable.add(composeGenderMyopiaStat(gradeCode.name(),
                    validConclusions.stream().filter(x -> gradeCode.getCode().equals(x.getSchoolGradeCode())).collect(Collectors.toList())));
        }
        schoolGradeGenderMyopiaTable.add(composeGenderMyopiaStat(TABLE_LABEL_TOTAL, validConclusions));
        return schoolGradeGenderMyopiaTable;
    }

    /**
     * 构造 学龄/视力低下程度 描述
     *
     * @param schoolAgeLowVisionLevelTable
     * @return
     */
    private Map<String, Object> composeSchoolAgeLowVisionLevelDesc(List<Map<String, Object>> schoolAgeLowVisionLevelTable) {
        int size = schoolAgeLowVisionLevelTable.size();
        Map<String, Object> conclusionDesc = new HashMap<>();
        List<BasicStatParams> schoolAgeLowVisionRatio = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Map<String, Object> stats = schoolAgeLowVisionLevelTable.get(i);
            List<BasicStatParams> list = (List<BasicStatParams>) stats.get("list");
            Float ratio = list.get(list.size() - 1).getRatio();
            if (i != size - 1) {
                String name = (String) stats.get("name");
                schoolAgeLowVisionRatio.add(new BasicStatParams(name, ratio, null));
            } else {
                conclusionDesc.put("totalAverageVision", stats.get(TABLE_LABEL_AVERAGE_VISION));
                conclusionDesc.put("totalLowVisionRatio", ratio);
            }
        }
        schoolAgeLowVisionRatio.sort(Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        conclusionDesc.put("sortedList", schoolAgeLowVisionRatio);
        conclusionDesc.put("schoolAgeLowVisionLevelTable", schoolAgeLowVisionLevelTable);
        return conclusionDesc;
    }

    /**
     * 构造 学龄/性别/视力情况 描述
     *
     * @param title 标题
     * @param schoolAgeGenderVisionTable  学龄视力表
     * @return
     */
    private Map<String, Object> composeSchoolAgeGenderVisionDesc(
            String title, List<Map<String, Object>> schoolAgeGenderVisionTable) {
        int size = schoolAgeGenderVisionTable.size();
        Map<String, Object> totalConclusion = schoolAgeGenderVisionTable.get(size - 1);
        List<TableBasicStatParams> list = (List<TableBasicStatParams>) totalConclusion.get("list");
        Map<String, Object> conclusionDesc = this.composeGenderRatio(list);
        conclusionDesc.put(title, schoolAgeGenderVisionTable);
        return conclusionDesc;
    }

    /**
     * 构造 学龄/性别/视力矫正情况 描述
     * @param title 表格描述
     * @param schoolAgeGenderVisionCorrectionTable 表格数据
     * @return
     */
    private Map<String, Object> composeSchoolAgeGenderVisionCorrectionDesc(
            String title, List<Map<String, Object>> schoolAgeGenderVisionCorrectionTable) {
        int size = schoolAgeGenderVisionCorrectionTable.size();
        Map<String, Object> totalConclusion = schoolAgeGenderVisionCorrectionTable.get(size - 1);
        List<TableBasicStatParams> totalList =
                (List<TableBasicStatParams>) totalConclusion.get("list");
        int primaryToHighTotalNum = 0;
        int primaryToHighCorrectionNum = 0;
        int primaryToHighCorrectionRatio = 0;
        List<BasicStatParams> sortedList = new ArrayList<>();
        for (Map<String, Object> item : schoolAgeGenderVisionCorrectionTable) {
            String schoolAgeName = (String) item.get("name");
            if (schoolAgeName.equals(TABLE_LABEL_TOTAL)) {
                continue;
            }
            SchoolAge schoolAge = SchoolAge.valueOf(schoolAgeName);
            List<TableBasicStatParams> list = (List<TableBasicStatParams>) item.get("list");
            TableBasicStatParams params =
                    list.stream().filter(x -> x.getTitle().equals(TABLE_LABEL_TOTAL)).findFirst().get();
            sortedList.add(new BasicStatParams(schoolAgeName, params.getRatio(), null));
            switch (schoolAge) {
                case KINDERGARTEN:
                    break;
                case PRIMARY:
                case JUNIOR:
                case HIGH:
                    primaryToHighTotalNum += params.getTotal();
                    primaryToHighCorrectionNum += params.getNum();
                    primaryToHighCorrectionRatio += params.getRatio();
                    break;
                case VOCATIONAL_HIGH:
                    break;
                default:
            }
        }
        sortedList.sort(Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        Map<String, Object> conclusionDesc = new HashMap<>();
        conclusionDesc.put("totalList", totalList);
        conclusionDesc.put("sortedList", sortedList);
        conclusionDesc.put("primaryToHighTotalNum", primaryToHighTotalNum);
        conclusionDesc.put("primaryToHighCorrectionNum", primaryToHighCorrectionNum);
        conclusionDesc.put("primaryToHighCorrectionRatio", primaryToHighCorrectionRatio);
        conclusionDesc.put(title, schoolAgeGenderVisionCorrectionTable);
        return conclusionDesc;
    }

    /**
     * 构造 学龄/预警级别 描述
     * @param title 表格描述
     * @param schoolAgeWarningLevelTable 表格数据
     * @return
     */
    private Map<String, Object> composeSchoolAgeWarningLevelDesc(
            String title, List<Map<String, Object>> schoolAgeWarningLevelTable) {
        int size = schoolAgeWarningLevelTable.size();
        Map<String, Object> totalStat = schoolAgeWarningLevelTable.get(size - 1);
        Long totalNum = (Long) totalStat.get(TABLE_LABEL_ROW_TOTAL);
        List<BasicStatParams> warningLevelZeroSchoolAgeList = new ArrayList<>();
        List<BasicStatParams> warningLevelOneSchoolAgeList = new ArrayList<>();
        List<BasicStatParams> warningLevelTwoSchoolAgeList = new ArrayList<>();
        List<BasicStatParams> warningLevelThreeSchoolAgeList = new ArrayList<>();
        for (int i = 0; i < size - 1; i++) {
            Map<String, Object> schoolAgeStat = schoolAgeWarningLevelTable.get(i);
            String schoolAgeName = (String) schoolAgeStat.get("name");
            List<BasicStatParams> list = (List<BasicStatParams>) schoolAgeStat.get("list");
            for (BasicStatParams params : list) {
                WarningLevel wl = WarningLevel.valueOf(params.getTitle());
                switch (wl) {
                    case ZERO:
                        warningLevelZeroSchoolAgeList.add(new BasicStatParams(schoolAgeName, params.getRatio(),
                                params.getNum()));
                        break;
                    case ONE:
                        warningLevelOneSchoolAgeList.add(new BasicStatParams(schoolAgeName, params.getRatio(),
                                params.getNum()));
                        break;
                    case TWO:
                        warningLevelTwoSchoolAgeList.add(new BasicStatParams(schoolAgeName, params.getRatio(),
                                params.getNum()));
                        break;
                    case THREE:
                        warningLevelThreeSchoolAgeList.add(new BasicStatParams(schoolAgeName, params.getRatio(),
                                params.getNum()));
                }
            }
        }
        warningLevelZeroSchoolAgeList.sort(Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        warningLevelOneSchoolAgeList.sort(Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        warningLevelTwoSchoolAgeList.sort(Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        warningLevelThreeSchoolAgeList.sort(Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        List<List<BasicStatParams>> sortedList = new ArrayList<>();
        sortedList.add(warningLevelZeroSchoolAgeList);
        sortedList.add(warningLevelOneSchoolAgeList);
        sortedList.add(warningLevelTwoSchoolAgeList);
        sortedList.add(warningLevelThreeSchoolAgeList);

        Map<String, Object> conclusionDesc = new HashMap<>();

        List<BasicStatParams> totalRowList = (List<BasicStatParams>) totalStat.get("list");
        long totalWarningNum = totalRowList.stream().mapToLong(x -> x.getNum()).sum();
        conclusionDesc.put("totalNum", totalWarningNum);
        conclusionDesc.put(TABLE_LABEL_TOTAL_RATIO, convertToPercentage(totalWarningNum * 1f / totalNum));

        List<ClassStat> levelList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            BasicStatParams params = totalRowList.get(i);
            levelList.add(new ClassStat(
                    params.getTitle(), params.getRatio(), params.getNum(), sortedList.get(i)));
        }
        conclusionDesc.put("levelList", levelList);
        conclusionDesc.put(title, schoolAgeWarningLevelTable);
        return conclusionDesc;
    }

    /**
     * 构造 学龄/视力情况 描述
     *
     * @param title 标题
     * @param schoolAgeMyopiaLevelTable 学龄期近视水平表
     * @return Map<String, Object>
     */
    private Map<String, Object> composeSchoolAgeMyopiaLevelDesc(
            String title, List<Map<String, Object>> schoolAgeMyopiaLevelTable) {
        int size = schoolAgeMyopiaLevelTable.size();
        List<BasicStatParams> schoolAgeMyopiaRatio = new ArrayList<>();
        Map<String, Object> conclusionDesc = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Map<String, Object> stats = schoolAgeMyopiaLevelTable.get(i);
            List<BasicStatParams> list = (List<BasicStatParams>) stats.get("list");
            Float ratio = list.get(list.size() - 1).getRatio();
            if (i != size - 1) {
                String name = (String) stats.get("name");
                schoolAgeMyopiaRatio.add(new BasicStatParams(name, ratio, null));
            }
        }
        schoolAgeMyopiaRatio.sort(Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        conclusionDesc.put("sortedList", schoolAgeMyopiaRatio);
        conclusionDesc.put(title, schoolAgeMyopiaLevelTable);
        return conclusionDesc;
    }

    /**
     * 构造 学龄/视力情况 描述
     * @param title 标题
     * @param schoolAgeGlassesTypeTable  学龄眼镜类型表
     * @return
     */
    private Map<String, Object> composeSchoolAgeGlassesTypeDesc(
            String title, List<Map<String, Object>> schoolAgeGlassesTypeTable) {
        int size = schoolAgeGlassesTypeTable.size();
        Map<String, Object> conclusionDesc = new HashMap<>();
        Map<String, Object> totalStat = schoolAgeGlassesTypeTable.get(size - 1);
        List<BasicStatParams> list = (List<BasicStatParams>) totalStat.get("list");
        Long wearingNum = 0L;
        float wearingRatio = 0;
        for (BasicStatParams params : list) {
            if (GlassesType.NOT_WEARING.name().equals(params.getTitle())) {
                continue;
            }
            wearingNum += params.getNum();
            wearingRatio += params.getRatio();
        }
        conclusionDesc.put("wearingNum", wearingNum);
        conclusionDesc.put("wearingRatio", round2Digits(wearingRatio));
        conclusionDesc.put("list", list);
        conclusionDesc.put(title, schoolAgeGlassesTypeTable);
        return conclusionDesc;
    }

    /**
     * 构造 性别 占比
     * @param list
     * @return
     */
    private Map<String, Object> composeGenderRatio(List<TableBasicStatParams> list) {
        float maleRatio =
                list.stream().filter(x -> GenderEnum.MALE.name().equals(x.getTitle())).map(BasicStatParams::getRatio).findFirst().get();
        float femaleRatio =
                list.stream().filter(x -> GenderEnum.FEMALE.name().equals(x.getTitle())).map(BasicStatParams::getRatio).findFirst().get();
        float totalRatio =
                list.stream().filter(x -> x.getTitle().equals(TABLE_LABEL_TOTAL)).map(BasicStatParams::getRatio).findFirst().get();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("maleRatio", maleRatio);
        resultMap.put("femaleRatio", femaleRatio);
        resultMap.put(TABLE_LABEL_TOTAL_RATIO, totalRatio);
        return resultMap;
    }

    public StatWholeResultDTO getPlanStatData(Integer planId) {

        ScreeningPlan sp = screeningPlanService.getById(planId);

        List<ScreeningPlanSchoolStudent> screenPlanSchoolStudent = screeningPlanSchoolStudentService.getByScreeningPlanId(planId);
        Map<Integer, Long> planSchoolStudentMap = screenPlanSchoolStudent.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getSchoolId, Collectors.counting()));
        Map<Integer, Long> planSchoolAgeStudentMap = screenPlanSchoolStudent.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getGradeType, Collectors.counting()));

        // 预计需要筛查的学生数
        long planStudentNum = screenPlanSchoolStudent.stream().count();
        // 通过筛查数据进行统计
        StatConclusionQueryDTO query = new StatConclusionQueryDTO();
        query.setPlanId(planId);
        List<StatConclusion> statConclusions = statConclusionService.listByQuery(query);
        if (statConclusions == null || statConclusions.size() == 0) {
            return null;
        }
        List<Integer> schoolIds =
                statConclusions.stream().map(StatConclusion::getSchoolId).distinct().collect(Collectors.toList());
        List<School> schools = schoolService.getByIds(schoolIds);
        // 按拼音获取前三个学校名
        List<String> schoolExamples = schools.stream().map(School::getName).sorted(Collator.getInstance(java.util.Locale.CHINA)).limit(3).collect(Collectors.toList());
        StatBaseDTO statBase = new StatBaseDTO(statConclusions);
        Map<Integer, List<StatConclusion>> schoolFirstScreenMap = statBase.getFirstScreen().stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));
        Map<Integer, List<StatConclusion>> schoolValidMap = statBase.getValid().stream().collect(Collectors.groupingBy(StatConclusion::getSchoolId));

        // 实际筛查人数
        long actualScreeningNum = statBase.getFirstScreen().size();
        // 有效人数
        long validFirstScreeningNum = statBase.getValid().size();

        // 组装各学龄段信息
        StatBusinessSchoolAgeDTO businessSchoolAge = new StatBusinessSchoolAgeDTO(statBase);

        StatWholeResultDTO whole = StatWholeResultDTO.builder()
                .plan(sp)
                .schoolCount(schools.size())
                .planStudentNum(planStudentNum)
                .actualScreeningNum(actualScreeningNum)
                .validFirstScreeningNum(validFirstScreeningNum)
                .validRatio(convertToPercentage(validFirstScreeningNum * 1f / actualScreeningNum))
                .schoolAgeDistribution(businessSchoolAge.getSortedDistributionMap())
                .schoolExamples(schoolExamples)
                .schoolPersonnel(getSchoolPersonnel(schools, planSchoolStudentMap, schoolFirstScreenMap, schoolValidMap))
                .schoolAgePersonnel(getSchoolAgePersonne(planSchoolAgeStudentMap, businessSchoolAge))
                .myopia(getMyopiaRatio(statBase.getValid()))
                .visionCorrection(getVisionCorrection(statBase.getValid()))
                .warnLevel(getWarnLevel(statBase.getValid()))
                .genderMyopia(getGenderMyopia(new StatGenderDTO(statBase.getValid())))
                .schoolAgeMyopia(getSchoolAgeMyopia(businessSchoolAge))
                .schoolMyopia(getSchoolMyopia(schools, schoolValidMap))
                .gradeMyopia(getGradeMyopia(statBase.getValid()))
                .build();
        return whole;
    }

    /**
     * 获取各学校筛查人员信息
     * @param schools
     * @param planSchoolStudentMap
     * @param schoolFirstScreenMap
     * @param schoolValidMap
     * @return
     */
    private List<StatSchoolPersonnelDTO> getSchoolPersonnel(List<School> schools, Map<Integer, Long> planSchoolStudentMap,
                                                            Map<Integer, List<StatConclusion>> schoolFirstScreenMap, Map<Integer, List<StatConclusion>> schoolValidMap) {
        List<StatSchoolPersonnelDTO> schoolPersonnels = schools.stream().map(school -> {
            StatSchoolPersonnelDTO persionnel = new StatSchoolPersonnelDTO();
            persionnel.setName(school.getName())
                    .setPlanScreeningNum(planSchoolStudentMap.getOrDefault(school.getId(), 0L))
                    .setActualScreeningNum(schoolFirstScreenMap.getOrDefault(school.getId(), Collections.emptyList()).size())
                    .setValidFirstScreeningNum(schoolValidMap.getOrDefault(school.getId(), Collections.emptyList()).size());
            return persionnel;
        })
                .sorted(Comparator.comparing(StatSchoolPersonnelDTO::getActualScreeningNum).reversed())
                .collect(Collectors.toList());
        // 添加合计
        StatSchoolPersonnelDTO total = new StatSchoolPersonnelDTO();
        total.setName(TOTAL_CHINESE);
        for(StatSchoolPersonnelDTO persionnel : schoolPersonnels) {
            total.setPlanScreeningNum(total.getPlanScreeningNum() + persionnel.getPlanScreeningNum());
            total.setActualScreeningNum(total.getActualScreeningNum() + persionnel.getActualScreeningNum());
            total.setValidFirstScreeningNum(total.getValidFirstScreeningNum() + persionnel.getValidFirstScreeningNum());
        }
        schoolPersonnels.add(total);
        return schoolPersonnels;
    }

    /**
     * 获取各学段筛查人员信息
     * @return
     */
    private List<StatSchoolAgePersonnelDTO> getSchoolAgePersonne(Map<Integer, Long> planSchoolAgeStudentMap, StatBusinessSchoolAgeDTO businessSchoolAge) {

        return planSchoolAgeStudentMap.keySet().stream().sorted().map(gradeType -> {
            StatSchoolAgePersonnelDTO persionnel = new StatSchoolAgePersonnelDTO();
            String schoolAgeName = SchoolAge.get(gradeType).name();
            persionnel.setSchoolAge(schoolAgeName)
                    .setPlanScreeningNum(planSchoolAgeStudentMap.getOrDefault(gradeType, 0L))
                    .setActualScreeningNum(businessSchoolAge.getFirstScreenSchoolAgeNumMap().getOrDefault(schoolAgeName, 0))
                    .setValidFirstScreeningNum(businessSchoolAge.getValidSchoolAgeNumMap().getOrDefault(schoolAgeName, 0));
            return persionnel;
        }).collect(Collectors.toList());
    }

    /**
     * 获取近视率、视力低下率、平均视力
     * @return
     */
    private List<TypeRatioDTO> getMyopiaRatio(List<StatConclusion> validConclusions) {
        List<TypeRatioDTO> myopiaRatio = new ArrayList<>();
        Long myopiaNum = validConclusions.stream().filter(x -> x.getIsMyopia() || GlassesType.ORTHOKERATOLOGY.code.equals(x.getGlassesType())).count();
        Long lowVisionNum = validConclusions.stream().filter(x -> x.getIsLowVision() ).count();
        Float vision = validConclusions.stream().map(x -> x.getVisionL() + x.getVisionR()).reduce(0f, Float::sum);
        int countNum = validConclusions.size();
        myopiaRatio.add(TypeRatioDTO.getInstance(RatioEnum.MYOPIA.name(), myopiaNum, convertToPercentage(myopiaNum * 1f / countNum)));
        myopiaRatio.add(TypeRatioDTO.getInstance(RatioEnum.LOW_VISION.name(), lowVisionNum, convertToPercentage(lowVisionNum * 1f / countNum)));
        myopiaRatio.add(TypeRatioDTO.getInstance(RatioEnum.AVERAGE_VISION.name(), round2Digits(vision / (countNum * 2)), null));
        return myopiaRatio;
    }

    /**
     * 获取未矫率、戴镜率、欠矫率、足矫率
     * @return
     */
    private List<TypeRatioDTO> getVisionCorrection(List<StatConclusion> validConclusions) {
        List<TypeRatioDTO> visionCorrection = new ArrayList<>();
        Long uncorrectedNum = validConclusions.stream().filter(x -> com.wupol.myopia.business.common.utils.constant.VisionCorrection.UNCORRECTED.code.equals(x.getVisionCorrection())).count();
        Long wearingNum = validConclusions.stream().filter(x -> x.getGlassesType() > 0).count();
        Long underCorrectedNum = validConclusions.stream().filter(x -> com.wupol.myopia.business.common.utils.constant.VisionCorrection.UNDER_CORRECTED.code.equals(x.getVisionCorrection())).count();
        Long enoughCorrectedNum = validConclusions.stream().filter(x -> com.wupol.myopia.business.common.utils.constant.VisionCorrection.ENOUGH_CORRECTED.code.equals(x.getVisionCorrection())).count();
        int countNum = validConclusions.size();
        visionCorrection.add(TypeRatioDTO.getInstance(RatioEnum.UNCORRECTED.name(), uncorrectedNum, convertToPercentage(uncorrectedNum * 1f / countNum)));
        visionCorrection.add(TypeRatioDTO.getInstance(RatioEnum.WEARING_RATIO.name(), wearingNum, convertToPercentage(wearingNum * 1f / countNum)));
        visionCorrection.add(TypeRatioDTO.getInstance(RatioEnum.UNDER_CORRECTED.name(), underCorrectedNum,  convertToPercentage(underCorrectedNum * 1f / countNum)));
        visionCorrection.add(TypeRatioDTO.getInstance(RatioEnum.ENOUGH_CORRECTED.name(), enoughCorrectedNum, convertToPercentage(enoughCorrectedNum * 1f / countNum)));
        return visionCorrection;
    }

    /**
     * 获取预警率
     * @return
     */
    private List<TypeRatioDTO> getWarnLevel(List<StatConclusion> validConclusions) {
        List<TypeRatioDTO> warnLevel = new ArrayList<>();
        Map<Integer, Long> warningLevelMap = validConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        int countNum = validConclusions.size();
        warnLevel.add(TypeRatioDTO.getInstance(RatioEnum.WARNING_LEVEL_0.name(), warningLevelMap.getOrDefault(WarningLevel.ZERO.code, 0L), convertToPercentage(warningLevelMap.getOrDefault(WarningLevel.ZERO.code, 0L) * 1f / countNum)));
        warnLevel.add(TypeRatioDTO.getInstance(RatioEnum.WARNING_LEVEL_1.name(), warningLevelMap.getOrDefault(WarningLevel.ONE.code, 0L), convertToPercentage(warningLevelMap.getOrDefault(WarningLevel.ONE.code, 0L) * 1f / countNum)));
        warnLevel.add(TypeRatioDTO.getInstance(RatioEnum.WARNING_LEVEL_2.name(), warningLevelMap.getOrDefault(WarningLevel.TWO.code, 0L), convertToPercentage(warningLevelMap.getOrDefault(WarningLevel.TWO.code, 0L) * 1f / countNum)));
        warnLevel.add(TypeRatioDTO.getInstance(RatioEnum.WARNING_LEVEL_3.name(), warningLevelMap.getOrDefault(WarningLevel.THREE.code, 0L), convertToPercentage(warningLevelMap.getOrDefault(WarningLevel.THREE.code, 0L) * 1f / countNum)));
        return warnLevel;
    }

    /**
     * 获取男女近视信息
     * @param statGenderDTO
     * @return
     */
    private List<MyopiaDTO> getGenderMyopia(StatGenderDTO statGenderDTO) {
        Long maleMyopiaNum = statGenderDTO.getMale().stream().filter(x -> x.getIsMyopia()).count();
        Long femaleMyopiaNum = statGenderDTO.getFemale().stream().filter(x -> x.getIsMyopia()).count();
        return Arrays.asList(
                MyopiaDTO.getInstance(statGenderDTO.getMale().size(), GenderEnum.MALE.name(), maleMyopiaNum, convertToPercentage(maleMyopiaNum * 1f / statGenderDTO.getMale().size())),
                MyopiaDTO.getInstance(statGenderDTO.getFemale().size(), GenderEnum.FEMALE.name(), femaleMyopiaNum, convertToPercentage(femaleMyopiaNum * 1f / statGenderDTO.getFemale().size()))
        );
    }

    /**
     * 获取学段近视信息
     * @param businessSchoolAge
     * @return
     */
    private List<MyopiaDTO> getSchoolAgeMyopia(StatBusinessSchoolAgeDTO businessSchoolAge) {
        return businessSchoolAge.getValidSchoolAgeNumMap().keySet().stream()
                .map(x -> {
                    List<StatConclusion> stat = businessSchoolAge.getValidSchoolAgeMap().getOrDefault(x, Collections.emptyList());
                    Long myopiaNum = stat.stream().filter(s -> s.getIsMyopia()).count();
                    return MyopiaDTO.getInstance(stat.size(), businessSchoolAge.getValidSchoolAgeDistributionMap().getOrDefault(x, 0L),
                            x, myopiaNum, convertToPercentage(myopiaNum * 1f / stat.size()));
                }).collect(Collectors.toList());
    }

    /**
     * 获取学校近视信息
     * @param schools
     * @param schoolValidMap
     * @return
     */
    private List<MyopiaDTO> getSchoolMyopia(List<School> schools, Map<Integer, List<StatConclusion>> schoolValidMap) {
        List<MyopiaDTO> schoolMyopia = schools.stream()
                .filter(school -> CollectionUtils.isNotEmpty(schoolValidMap.get(school.getId())))
                .map(x -> {
                    List<StatConclusion> stat = schoolValidMap.get(x.getId());
                    Long myopiaNum = stat.stream().filter(s -> s.getIsMyopia()).count();
                    return MyopiaDTO.getInstance(stat.size(), x.getName(),
                            myopiaNum, convertToPercentage(myopiaNum * 1f / stat.size()));
                })
                .sorted(Comparator.comparing(MyopiaDTO::getRatio).reversed())
                .collect(Collectors.toList());
        // 设置排名
        for (int i = 0; i < schoolMyopia.size(); i++) {
            schoolMyopia.get(i).setRanking(i+1);
        }
        return schoolMyopia;
    }

    /**
     * 获取年级近视信息
     * @param validConclusions
     * @return
     */
    private List<MyopiaDTO> getGradeMyopia(List<StatConclusion> validConclusions) {
        Map<String, List<StatConclusion>> gradeMap = validConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolGradeCode));
        return gradeMap.keySet().stream()
                .map(x -> {
                    List<StatConclusion> stat = gradeMap.get(x);
                    Long myopiaNum = stat.stream().filter(s -> s.getIsMyopia()).count();
                    return MyopiaDTO.getInstance(stat.size(), GradeCodeEnum.getByCode(x).name(),
                            myopiaNum, convertToPercentage(myopiaNum * 1f / stat.size()));
                }).collect(Collectors.toList());
    }


    /**
     * 获取学校筛查报告数据
     *
     * @param srcScreeningNoticeId 通知ID
     * @param planId               计划IDmz
     * @param schoolId             学校ID
     * @return
     */
    public Map<String, Object> getSchoolStatData(Integer srcScreeningNoticeId, Integer planId, int schoolId) {
        Date startDate;
        Date endDate;
        long planStudentNum;
        StatConclusionQueryDTO query = new StatConclusionQueryDTO();
        query.setSchoolId(schoolId);
        if (srcScreeningNoticeId != null) {
            ScreeningNotice notice = screeningNoticeService.getById(srcScreeningNoticeId);
            query.setSrcScreeningNoticeId(srcScreeningNoticeId);
            startDate = notice.getStartTime();
            endDate = notice.getEndTime();
            planStudentNum = screeningPlanSchoolStudentService.countPlanSchoolStudent(srcScreeningNoticeId, schoolId);
        } else if (planId != null) {
            query.setPlanId(planId);
            ScreeningPlan sp = screeningPlanService.getById(planId);
            startDate = sp.getStartTime();
            endDate = sp.getEndTime();
            planStudentNum = screeningPlanSchoolStudentService.getByScreeningPlanId(planId)
                    .stream()
                    .filter(x -> x.getSchoolId() == schoolId)
                    .count();
        } else {
            throw new ParameterNotFoundException("Parameters not illegal");
        }
        List<StatConclusion> statConclusions = statConclusionService.listByQuery(query);
        if (statConclusions == null) {
            return null;
        }
        School school = schoolService.getById(schoolId);
        String schoolName = school.getName();

        StatBaseDTO statBase = new StatBaseDTO(statConclusions);
        List<StatConclusion> validConclusions = statBase.getValid();
        // 组装性别信息
        StatGenderDTO statGender = new StatGenderDTO(validConclusions);

        long totalFirstScreeningNum = statBase.getFirstScreen().size();
        long validFirstScreeningNum = validConclusions.size();
        long myopiaNum = validConclusions.stream().filter(x -> x.getIsMyopia() != null && x.getIsMyopia()).count();
        long lowVisionNum =
                validConclusions.stream().filter(x -> x.getIsLowVision() != null && x.getIsLowVision()).count();
        long warning0Num =
                validConclusions.stream().filter(x -> WarningLevel.ZERO.code.equals(x.getWarningLevel())).count();
        long warning1Num =
                validConclusions.stream().filter(x -> WarningLevel.ONE.code.equals(x.getWarningLevel())).count();
        long warning2Num =
                validConclusions.stream().filter(x -> WarningLevel.TWO.code.equals(x.getWarningLevel())).count();
        long warning3Num =
                validConclusions.stream().filter(x -> WarningLevel.THREE.code.equals(x.getWarningLevel())).count();
        long warningNum = warning0Num + warning1Num + warning2Num + warning3Num;

        AverageVision averageVision = this.calculateAverageVision(validConclusions);
        float averageVisionValue =
                round2Digits((averageVision.getAverageVisionLeft() + averageVision.getAverageVisionRight()) / 2);

        List<SchoolGradeItemsDTO> schoolGradeItems = schoolGradeService.getAllGradeList(schoolId);

        List<StatConclusionReportDTO> statConclusionReportDTOs =
                statConclusionService.getReportVo(srcScreeningNoticeId, planId, schoolId);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("schoolName", schoolName);
        resultMap.put("actualScreeningNum", totalFirstScreeningNum);
        resultMap.put("validFirstScreeningNum", validFirstScreeningNum);
        resultMap.put("maleNum", statGender.getMale().size());
        resultMap.put("femaleNum", statGender.getFemale().size());
        resultMap.put("myopiaRatio", convertToPercentage(myopiaNum * 1f / validFirstScreeningNum));
        resultMap.put("lowVisionRatio",
                convertToPercentage(lowVisionNum * 1f / validFirstScreeningNum));
        resultMap.put(TABLE_LABEL_AVERAGE_VISION, averageVisionValue);
        List<BasicStatParams> basicStatParamsList = new ArrayList<>();
        basicStatParamsList.add(new BasicStatParams("warningTotal",
                convertToPercentage(warningNum * 1f / validFirstScreeningNum),
                warningNum));
        basicStatParamsList.add(new BasicStatParams("warning0",
                convertToPercentage(warning0Num * 1f / validFirstScreeningNum),
                warning0Num));
        basicStatParamsList.add(new BasicStatParams("warning1",
                convertToPercentage(warning1Num * 1f / validFirstScreeningNum),
                warning1Num));
        basicStatParamsList.add(new BasicStatParams("warning2",
                convertToPercentage(warning2Num * 1f / validFirstScreeningNum),
                warning2Num));
        basicStatParamsList.add(new BasicStatParams("warning3",
                convertToPercentage(warning3Num * 1f / validFirstScreeningNum),
                warning3Num));
        List<GradeCodeEnum> gradeCodeEnumList = SchoolUtil.getGradeCodeEnumListBySchoolType(SchoolEnum.getByType(school.getType()));
        resultMap.put("warningLevelStat", basicStatParamsList);
        resultMap.put("genderLowVisionLevelDesc", composeGenderLowVisionLevelDesc(validConclusions));
        resultMap.put("schoolGradeLowVisionLevelDesc", composeSchoolGradeLowVisionLevelDesc(validConclusions, gradeCodeEnumList));
        resultMap.put("schoolGradeMyopiaLevelDesc", composeSchoolGradeMyopiaLevelDesc(validConclusions, gradeCodeEnumList));
        resultMap.put("schoolGradeClassLowVisionLevelTable", composeSchoolGradeClassLowVisionLevelTable(schoolGradeItems, validConclusions));
        resultMap.put("schoolGradeClassMyopiaLevelTable", composeSchoolGradeClassMyopiaLevelTable(schoolGradeItems, validConclusions));
        resultMap.put("genderMyopiaLevelDesc", composeGenderMyopiaLevelDesc(validConclusions));
        resultMap.put("schoolGradeWearingTypeDesc", composeSchoolGradeWearingTypeDesc(schoolGradeItems, validConclusions));
        resultMap.put("schoolGradeGenderUncorrectedDesc", composeSchoolGradeGenderUncorrectedDesc(schoolGradeItems, validConclusions));
        resultMap.put("schoolGradeGenderUnderCorrectedDesc", composeSchoolGradeGenderUnderCorrectedDesc(schoolGradeItems, validConclusions));
        resultMap.put("schoolGradeWarningLevelDesc", composeSchoolGradeWarningLevelDesc(schoolGradeItems, validConclusions));
        resultMap.put("schoolClassStudentStatList", composeSchoolClassStudentStatList(schoolGradeItems, statConclusionReportDTOs));
        resultMap.put("startDate", startDate.getTime());
        resultMap.put("endDate", endDate.getTime());
        resultMap.put("planStudentNum", planStudentNum);
        return resultMap;
    }

    /**
     * 构建 学校每个班级的学生详情
     *
     * @param schoolGradeItemList 学校班级列表
     * @return
     */
    private List<Map<String, Object>> composeSchoolClassStudentStatList(
            List<SchoolGradeItemsDTO> schoolGradeItemList,
            List<StatConclusionReportDTO> statConclusionReportDTOs) {
        List<Map<String, Object>> schoolStudentStatList = new ArrayList<>();
        for (SchoolGradeItemsDTO schoolGradeItems : schoolGradeItemList) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeItems.getGradeCode());
            List<SchoolClass> schoolClasses = schoolGradeItems.getChild();
            List<Map<String, Object>> schoolClassStatList = new ArrayList<>();
            for (SchoolClass schoolClass : schoolClasses) {
                List<StatConclusionReportDTO> studentStatList =
                        statConclusionReportDTOs.stream()
                                .filter(x
                                        -> gradeCodeEnum.getCode().equals(x.getSchoolGradeCode())
                                                && schoolClass.getName().equals(x.getClassName()))
                                .collect(Collectors.toList());
                Map<String, Object> schoolClassStat = new HashMap<>();
                schoolClassStat.put("name", schoolClass.getName());
                schoolClassStat.put("list", genVisionScreeningResultReportDTOs(studentStatList));
                schoolClassStatList.add(schoolClassStat);
            }
            Map<String, Object> gradeStat = new HashMap<>();
            gradeStat.put("name", gradeCodeEnum.name());
            gradeStat.put("list", schoolClassStatList);
            schoolStudentStatList.add(gradeStat);
        }
        return schoolStudentStatList;
    }

    /**
     *
     * 构建 年级 预警级别 统计
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeSchoolGradeWarningLevelDesc(
            List<SchoolGradeItemsDTO> schoolGradeItemList, List<StatConclusion> statConclusions) {
        List<Map<String, Object>> schoolGradeGenderVisionTable = new ArrayList<>();
        for (SchoolGradeItemsDTO schoolGradeItems : schoolGradeItemList) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeItems.getGradeCode());
            List<StatConclusion> list =
                    statConclusions.stream()
                            .filter(x -> gradeCodeEnum.getCode().equals(x.getSchoolGradeCode()))
                            .collect(Collectors.toList());
            schoolGradeGenderVisionTable.add(composeWarningLevelStat(gradeCodeEnum.name(), list));
        }
        Map<String, Object> totalStat = composeWarningLevelStat(TABLE_LABEL_TOTAL, statConclusions);
        schoolGradeGenderVisionTable.add(totalStat);
        List<BasicStatParams> totalStatList = (List<BasicStatParams>) totalStat.get("list");
        long totalWarningNum = totalStatList.stream().mapToLong(BasicStatParams::getNum).sum();
        long totalSize = statConclusions.size();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", schoolGradeGenderVisionTable);
        resultMap.put(TABLE_LABEL_TOTAL_STAT_LIST, totalStatList);
        resultMap.put("totalWarningNum", totalWarningNum);
        resultMap.put("totalWarningRatio", convertToPercentage(totalWarningNum * 1f / totalSize));
        return resultMap;
    }

    /**
     * 构建 年级 性别 视力未矫情况 统计
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeSchoolGradeGenderUncorrectedDesc(
            List<SchoolGradeItemsDTO> schoolGradeItemList, List<StatConclusion> statConclusions) {
        List<StatConclusion> myopiaConclusions =
                statConclusions.stream().filter(x -> x.getIsMyopia() != null && x.getIsMyopia()).collect(Collectors.toList());
        List<Map<String, Object>> schoolGradeGenderVisionTable = new ArrayList<>();
        for (SchoolGradeItemsDTO schoolGradeItems : schoolGradeItemList) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeItems.getGradeCode());
            List<StatConclusion> list =
                    myopiaConclusions.stream().filter(x -> gradeCodeEnum.getCode().equals(x.getSchoolGradeCode())).collect(Collectors.toList());
            schoolGradeGenderVisionTable.add(composeGenderVisionUncorrectedStat(gradeCodeEnum.name(), list));
        }
        Map<String, Object> totalStat = composeGenderVisionUncorrectedStat(TABLE_LABEL_TOTAL, myopiaConclusions);
        schoolGradeGenderVisionTable.add(totalStat);
        List<BasicStatParams> totalStatList = (List<BasicStatParams>) totalStat.get("list");
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", schoolGradeGenderVisionTable);
        resultMap.put(TABLE_LABEL_TOTAL_STAT_LIST, totalStatList);
        return resultMap;
    }

    /**
     * 构建 年级 性别 视力欠矫情况 统计
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeSchoolGradeGenderUnderCorrectedDesc(
            List<SchoolGradeItemsDTO> schoolGradeItemList, List<StatConclusion> statConclusions) {
        List<StatConclusion> myopiaConclusions =
                statConclusions.stream().filter(StatConclusion::getIsMyopia).collect(Collectors.toList());
        List<Map<String, Object>> schoolGradeGenderVisionTable = new ArrayList<>();
        for (SchoolGradeItemsDTO schoolGradeItems : schoolGradeItemList) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeItems.getGradeCode());
            List<StatConclusion> list =
                    myopiaConclusions.stream().filter(x -> gradeCodeEnum.getCode().equals(x.getSchoolGradeCode())).collect(Collectors.toList());
            schoolGradeGenderVisionTable.add(composeGenderVisionUnderCorrectedStat(gradeCodeEnum.name(), list));
        }
        Map<String, Object> totalStat = composeGenderVisionUnderCorrectedStat(TABLE_LABEL_TOTAL, myopiaConclusions);
        schoolGradeGenderVisionTable.add(totalStat);
        List<BasicStatParams> totalStatList = (List<BasicStatParams>) totalStat.get("list");
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", schoolGradeGenderVisionTable);
        resultMap.put(TABLE_LABEL_TOTAL_STAT_LIST, totalStatList);
        return resultMap;
    }

    /**
     * 构建 年级 戴镜情况 统计
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeSchoolGradeWearingTypeDesc(
            List<SchoolGradeItemsDTO> schoolGradeItemList, List<StatConclusion> statConclusions) {
        List<Map<String, Object>> schoolGradeWearingTypeTable = new ArrayList<>();
        for (SchoolGradeItemsDTO schoolGradeItems : schoolGradeItemList) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeItems.getGradeCode());
            List<StatConclusion> list =
                    statConclusions.stream().filter(x -> gradeCodeEnum.getCode().equals(x.getSchoolGradeCode())).collect(Collectors.toList());
            schoolGradeWearingTypeTable.add(composeGlassesTypeStat(gradeCodeEnum.name(), list));
        }
        Map<String, Object> totalStat = composeGlassesTypeStat(TABLE_LABEL_TOTAL, statConclusions);
        schoolGradeWearingTypeTable.add(totalStat);

        long totalSize = statConclusions.size();
        long totalWearingNum =
                statConclusions.stream().filter(x -> x.getGlassesType() != null && !GlassesType.NOT_WEARING.code.equals(x.getGlassesType())).count();
        List<BasicStatParams> totalStatList = (List<BasicStatParams>) totalStat.get("list");
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", schoolGradeWearingTypeTable);
        resultMap.put("totalWearingNum", totalWearingNum);
        resultMap.put("totalWearingRatio", convertToPercentage(totalWearingNum * 1f / totalSize));
        resultMap.put(TABLE_LABEL_TOTAL_STAT_LIST, totalStatList);
        return resultMap;
    }

    /**
     * 构建 性别 视力低下 统计
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeGenderLowVisionLevelDesc(
            List<StatConclusion> statConclusions) {
        List<StatConclusion> maleList =
                statConclusions.stream()
                        .filter(x -> GenderEnum.MALE.type.equals(x.getGender()))
                        .collect(Collectors.toList());
        List<StatConclusion> femaleList =
                statConclusions.stream()
                        .filter(x -> GenderEnum.FEMALE.type.equals(x.getGender()))
                        .collect(Collectors.toList());
        Map<String, Object> maleStat = composeLowVisionLevelStat(GenderEnum.MALE.name(), maleList);
        Map<String, Object> femaleStat =
                composeLowVisionLevelStat(GenderEnum.FEMALE.name(), femaleList);
        Map<String, Object> totalStat = composeLowVisionLevelStat(TABLE_LABEL_TOTAL, statConclusions);
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(maleStat);
        list.add(femaleStat);
        list.add(totalStat);
        int totalSize = totalStat.size();
        List<BasicStatParams> totalLevelStat = (List<BasicStatParams>) totalStat.get("list");
        BasicStatParams lastTotalLevelStat = totalLevelStat.get(totalSize - 1);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", list);
        resultMap.put(TABLE_LABEL_AVERAGE_VISION, totalStat.get(TABLE_LABEL_AVERAGE_VISION));
        resultMap.put(TABLE_LABEL_TOTAL_RATIO, lastTotalLevelStat.getRatio());
        resultMap.put("topStat", getTopStatList(totalLevelStat.subList(0, totalSize - 1)));
        return resultMap;
    }

    /**
     * 构建 性别 近视 统计
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeGenderMyopiaLevelDesc(List<StatConclusion> statConclusions) {
        List<StatConclusion> maleList =
                statConclusions.stream()
                        .filter(x -> GenderEnum.MALE.type.equals(x.getGender()))
                        .collect(Collectors.toList());
        List<StatConclusion> femaleList =
                statConclusions.stream()
                        .filter(x -> GenderEnum.FEMALE.type.equals(x.getGender()))
                        .collect(Collectors.toList());
        Map<String, Object> maleStat = composeMyopiaLevelStat(GenderEnum.MALE.name(), maleList);
        Map<String, Object> femaleStat =
                composeMyopiaLevelStat(GenderEnum.FEMALE.name(), femaleList);
        Map<String, Object> totalStat = composeMyopiaLevelStat(TABLE_LABEL_TOTAL, statConclusions);
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(maleStat);
        list.add(femaleStat);
        list.add(totalStat);
        List<BasicStatParams> totalLevelStat = (List<BasicStatParams>) totalStat.get("list");
        int totalLevelStatSize = totalLevelStat.size();
        BasicStatParams lastTotalLevelStat = totalLevelStat.get(totalLevelStatSize - 1);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", list);
        resultMap.put(TABLE_LABEL_TOTAL_RATIO, lastTotalLevelStat.getRatio());
        resultMap.put("topStat", getTopStatList(totalLevelStat.subList(0, totalLevelStatSize - 1)));
        return resultMap;
    }

    /**
     * 从list中获取占比最高的头部list
     * @param list
     * @return
     */
    private List<BasicStatParams> getTopStatList(List<BasicStatParams> list) {
        Map<Float, List<BasicStatParams>> map =
                list.stream().collect(Collectors.groupingBy(BasicStatParams::getRatio));
        return map.entrySet().stream().min(Map.Entry.comparingByKey(Comparator.reverseOrder())).get().getValue();
    }

    /**
     * 构建 年级 视力低下 统计
     *
     * @param statConclusions
     * @param gradeCodeEnumList
     * @return
     */
    private Map<String, Object> composeSchoolGradeMyopiaLevelDesc(
            List<StatConclusion> statConclusions, List<GradeCodeEnum> gradeCodeEnumList) {
        List<BasicStatParams> schoolGradeMyopiaRatioList = new ArrayList<>();
        List<Map<String, Object>> list = new ArrayList<>();
        for (GradeCodeEnum gradeCode : gradeCodeEnumList) {
            Map<String, Object> lowVisionLevelStat = composeMyopiaLevelStat(gradeCode.name(),
                    statConclusions.stream()
                            .filter(x -> gradeCode.getCode().equals(x.getSchoolGradeCode()))
                            .collect(Collectors.toList()));
            list.add(lowVisionLevelStat);
            List<BasicStatParams> paramsList =
                    (List<BasicStatParams>) lowVisionLevelStat.get("list");
            schoolGradeMyopiaRatioList.add(new BasicStatParams(
                    gradeCode.name(), paramsList.get(paramsList.size() - 1).getRatio(), null));
        }
        list.add(composeMyopiaLevelStat(TABLE_LABEL_TOTAL, statConclusions));
        schoolGradeMyopiaRatioList.sort(Comparator.comparingDouble(BasicStatParams::getRatio).reversed());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", list);
        resultMap.put("sortedStat", schoolGradeMyopiaRatioList);
        return resultMap;
    }

    /**
     * 构建 年级 视力低下 统计
     *
     * @param statConclusions
     * @param gradeCodeEnumList
     * @return
     */
    private Map<String, Object> composeSchoolGradeLowVisionLevelDesc(
            List<StatConclusion> statConclusions, List<GradeCodeEnum> gradeCodeEnumList) {
        List<BasicStatParams> schoolGradeLowVisionRatioList = new ArrayList<>();
        List<Map<String, Object>> list = new ArrayList<>();
        for (GradeCodeEnum gradeCode : gradeCodeEnumList) {
            Map<String, Object> lowVisionLevelStat = composeLowVisionLevelStat(gradeCode.name(),
                    statConclusions.stream()
                            .filter(x -> gradeCode.getCode().equals(x.getSchoolGradeCode()))
                            .collect(Collectors.toList()));
            list.add(lowVisionLevelStat);
            List<BasicStatParams> paramsList =
                    (List<BasicStatParams>) lowVisionLevelStat.get("list");
            schoolGradeLowVisionRatioList.add(new BasicStatParams(
                    gradeCode.name(), paramsList.get(paramsList.size() - 1).getRatio(), null));
        }
        list.add(composeLowVisionLevelStat(TABLE_LABEL_TOTAL, statConclusions));
        schoolGradeLowVisionRatioList.sort(Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", list);
        resultMap.put("sortedStat", schoolGradeLowVisionRatioList);
        return resultMap;
    }

    /**
     * 构建 年级 班级 视力低下 统计
     *
     * @param statConclusions
     * @return
     */
    private List<Map<String, Object>> composeSchoolGradeClassLowVisionLevelTable(
            List<SchoolGradeItemsDTO> schoolGradeItems, List<StatConclusion> statConclusions) {
        List<Map<String, Object>> classList = new ArrayList<>();
        int rowKey = 0;
        for (GradeCodeEnum gradeCode : GradeCodeEnum.values()) {
            SchoolGradeItemsDTO schoolGradeItem =
                    schoolGradeItems.stream()
                            .filter(x -> gradeCode.getCode().equals(x.getGradeCode()))
                            .findFirst()
                            .orElse(null);
            if (schoolGradeItem == null) {
                continue;
            }
            List<SchoolClass> schoolClasses = schoolGradeItem.getChild();
            for (SchoolClass schoolClass : schoolClasses) {
                Map<String, Object> lowVisionLevelStat = composeLowVisionLevelStat(
                        schoolClass.getName(),
                        statConclusions.stream()
                                .filter(x -> schoolClass.getName().equals(x.getSchoolClassName())
                                        && gradeCode.getCode().equals(x.getSchoolGradeCode()))
                                .collect(Collectors.toList()));
                lowVisionLevelStat.put("rowKey", ++rowKey);
                lowVisionLevelStat.put("grade", gradeCode.name());
                classList.add(lowVisionLevelStat);
            }
        }
        return classList;
    }

    /**
     * 构建 年级 班级 近视 统计
     * @param statConclusions
     * @return
     */
    private List<Map<String, Object>> composeSchoolGradeClassMyopiaLevelTable(
            List<SchoolGradeItemsDTO> schoolGradeItems, List<StatConclusion> statConclusions) {
        List<Map<String, Object>> classList = new ArrayList<>();
        int rowKey = 0;
        for (GradeCodeEnum gradeCode : GradeCodeEnum.values()) {
            SchoolGradeItemsDTO schoolGradeItem =
                    schoolGradeItems.stream()
                            .filter(x -> gradeCode.getCode().equals(x.getGradeCode()))
                            .findFirst()
                            .orElse(null);
            if (schoolGradeItem == null) {
                continue;
            }

            List<SchoolClass> schoolClasses = schoolGradeItem.getChild();
            for (SchoolClass schoolClass : schoolClasses) {
                Map<String, Object> myopiaLevelStat = composeMyopiaLevelStat(schoolClass.getName(),
                        statConclusions.stream()
                                .filter(x
                                        -> schoolClass.getName().equals(x.getSchoolClassName())
                                                && gradeCode.getCode().equals(
                                                        x.getSchoolGradeCode()))
                                .collect(Collectors.toList()));
                myopiaLevelStat.put("rowKey", ++rowKey);
                myopiaLevelStat.put("grade", gradeCode.name());
                classList.add(myopiaLevelStat);
            }
        }
        return classList;
    }

    /**
     * 构建 性别 视力低下 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeGenderLowVisionStat(
            String name, List<StatConclusion> statConclusions) {
        Predicate<StatConclusion> predicate = StatConclusion::getIsLowVision;
        return composeGenderPredicateStat(name, statConclusions, predicate);
    }

    /**
     * 构建 性别 近视 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeGenderMyopiaStat(
            String name, List<StatConclusion> statConclusions) {
        Predicate<StatConclusion> predicate =
                x -> x.getIsMyopia() || GlassesType.ORTHOKERATOLOGY.code.equals(x.getGlassesType());
        return composeGenderPredicateStat(name, statConclusions, predicate);
    }

    /**
     * 构建 学龄 视力未矫 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeGenderVisionUncorrectedStat(
            String name, List<StatConclusion> statConclusions) {
        Predicate<StatConclusion> predicate =
                x -> VisionCorrection.UNCORRECTED.code.equals(x.getVisionCorrection());
        return composeGenderPredicateStat(name, statConclusions, predicate);
    }

    /**
     * 构建 学龄 视力欠矫 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeGenderVisionUnderCorrectedStat(
            String name, List<StatConclusion> statConclusions) {
        Predicate<StatConclusion> predicate =
                x -> VisionCorrection.UNDER_CORRECTED.code.equals(x.getVisionCorrection());
        return composeGenderPredicateStat(name, statConclusions, predicate);
    }

    /**
     * 构建 学龄 性别 过滤条件 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @param predicate
     * @return
     */
    private Map<String, Object> composeGenderPredicateStat(String name,
            List<StatConclusion> statConclusions, Predicate<StatConclusion> predicate) {
        long rowTotal = statConclusions.size();

        List<StatConclusion> maleList =
                statConclusions.stream().filter(x -> GenderEnum.MALE.type.equals(x.getGender())).collect(Collectors.toList());

        List<StatConclusion> femaleList =
                statConclusions.stream().filter(x -> GenderEnum.FEMALE.type.equals(x.getGender())).collect(Collectors.toList());

        long maleLowVisionNum = maleList.stream().filter(predicate).count();
        long femaleLowVisionNum = femaleList.stream().filter(predicate).count();
        List<TableBasicStatParams> list = new ArrayList<>();
        list.add(composeTableBasicParams(GenderEnum.MALE.name(), maleLowVisionNum, maleList.size()));
        list.add(composeTableBasicParams(GenderEnum.FEMALE.name(), femaleLowVisionNum, femaleList.size()));
        list.add(composeTableBasicParams(TABLE_LABEL_TOTAL, maleLowVisionNum + femaleLowVisionNum, rowTotal));
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name", name);
        resultMap.put(TABLE_LABEL_ROW_TOTAL, rowTotal);
        resultMap.put("list", list);
        return resultMap;
    }

    /**
     * 构建 视力低下分级 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeLowVisionLevelStat(
            String name, List<StatConclusion> statConclusions) {
        Predicate<StatConclusion> levelOnePredicate =
                x -> WarningLevel.ONE.code.equals(x.getNakedVisionWarningLevel());
        Predicate<StatConclusion> levelTwoPredicate =
                x -> WarningLevel.TWO.code.equals(x.getNakedVisionWarningLevel());
        Predicate<StatConclusion> levelThreePredicate =
                x -> WarningLevel.THREE.code.equals(x.getNakedVisionWarningLevel());
        Map<String, Object> levelMap = composeLevelStat(
                name, statConclusions, levelOnePredicate, levelTwoPredicate, levelThreePredicate);
        AverageVision averageVision = this.calculateAverageVision(statConclusions);
        float averageVisionValue = round2Digits(
                (averageVision.getAverageVisionLeft() + averageVision.getAverageVisionRight()) / 2);
        levelMap.put(TABLE_LABEL_AVERAGE_VISION, averageVisionValue);
        return levelMap;
    }

    /**
     * 构建 近视分级 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeMyopiaLevelStat(
            String name, List<StatConclusion> statConclusions) {
        Predicate<StatConclusion> levelOnePredicate =
                x -> WarningLevel.ONE.code.equals(x.getMyopiaWarningLevel());
        Predicate<StatConclusion> levelTwoPredicate =
                x -> WarningLevel.TWO.code.equals(x.getMyopiaWarningLevel());
        Predicate<StatConclusion> levelThreePredicate =
                x -> WarningLevel.THREE.code.equals(x.getMyopiaWarningLevel());
        return composeLevelStat(name, statConclusions, levelOnePredicate, levelTwoPredicate, levelThreePredicate);
    }

    /**
     * 构建 学龄 预警级别 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeWarningLevelStat(
            String name, List<StatConclusion> statConclusions) {
        long rowTotal = statConclusions.size();
        long levelZeroNum =
                statConclusions.stream().filter(x -> WarningLevel.ZERO.code.equals(x.getWarningLevel())).count();
        long levelOneNum =
                statConclusions.stream().filter(x -> WarningLevel.ONE.code.equals(x.getWarningLevel())).count();
        long levelTwoNum =
                statConclusions.stream().filter(x -> WarningLevel.TWO.code.equals(x.getWarningLevel())).count();
        long levelThreeNum =
                statConclusions.stream().filter(x -> WarningLevel.THREE.code.equals(x.getWarningLevel())).count();
        List<BasicStatParams> list = new ArrayList<>();
        list.add(composeBasicParams(WarningLevel.ZERO.name(), levelZeroNum, rowTotal));
        list.add(composeBasicParams(WarningLevel.ONE.name(), levelOneNum, rowTotal));
        list.add(composeBasicParams(WarningLevel.TWO.name(), levelTwoNum, rowTotal));
        list.add(composeBasicParams(WarningLevel.THREE.name(), levelThreeNum, rowTotal));
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name", name);
        resultMap.put(TABLE_LABEL_ROW_TOTAL, rowTotal);
        resultMap.put("list", list);
        return resultMap;
    }

    /**
     * 构建 预警级别 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @param levelOnePredicate 一级过滤条件
     * @param levelTwoPredicate 二级过滤条件
     * @param levelThreePredicate 三级过滤条件
     * @return
     */
    private Map<String, Object> composeLevelStat(String name, List<StatConclusion> statConclusions,
            Predicate<StatConclusion> levelOnePredicate,
            Predicate<StatConclusion> levelTwoPredicate,
            Predicate<StatConclusion> levelThreePredicate) {
        long rowTotal = statConclusions.size();
        long levelOneNum = statConclusions.stream().filter(levelOnePredicate).count();
        long levelTwoNum = statConclusions.stream().filter(levelTwoPredicate).count();
        long levelThreeNum = statConclusions.stream().filter(levelThreePredicate).count();
        List<BasicStatParams> list = new ArrayList<>();
        list.add(composeBasicParams(WarningLevel.ONE.name(), levelOneNum, rowTotal));
        list.add(composeBasicParams(WarningLevel.TWO.name(), levelTwoNum, rowTotal));
        list.add(composeBasicParams(WarningLevel.THREE.name(), levelThreeNum, rowTotal));
        list.add(composeBasicParams("levelTotal", levelOneNum + levelTwoNum + levelThreeNum, rowTotal));
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name", name);
        resultMap.put(TABLE_LABEL_ROW_TOTAL, rowTotal);
        resultMap.put("list", list);
        return resultMap;
    }

    /**
     * 构建 戴镜类型 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeGlassesTypeStat(
            String name, List<StatConclusion> statConclusions) {
        long rowTotal = statConclusions.size();
        long typeFrameNum =
                statConclusions.stream().filter(x -> GlassesType.FRAME_GLASSES.code.equals(x.getGlassesType())).count();
        long typeContactLensNum =
                statConclusions.stream().filter(x -> GlassesType.CONTACT_LENS.code.equals(x.getGlassesType())).count();
        long typeOrthokeratologyNum =
                statConclusions.stream().filter(x -> GlassesType.ORTHOKERATOLOGY.code.equals(x.getGlassesType())).count();
        long typeNotWearingNum =
                statConclusions.stream().filter(x -> GlassesType.NOT_WEARING.code.equals(x.getGlassesType())).count();
        List<BasicStatParams> list = new ArrayList<>();
        list.add(composeBasicParams(GlassesType.FRAME_GLASSES.name(), typeFrameNum, rowTotal));
        list.add(composeBasicParams(GlassesType.FRAME_GLASSES.name(), typeContactLensNum, rowTotal));
        list.add(composeBasicParams(GlassesType.ORTHOKERATOLOGY.name(), typeOrthokeratologyNum, rowTotal));
        list.add(composeBasicParams(GlassesType.NOT_WEARING.name(), typeNotWearingNum, rowTotal));
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("name", name);
        resultMap.put(TABLE_LABEL_ROW_TOTAL, rowTotal);
        resultMap.put("list", list);
        return resultMap;
    }

    /**
     * 生成筛查数据
     * @param statConclusionExportVos
     * @return
     */
    private List<VisionScreeningResultReportDTO> genVisionScreeningResultReportDTOs(
            List<StatConclusionReportDTO> statConclusionExportVos) {
        List<VisionScreeningResultReportDTO> reportVos = new ArrayList<>();
        for (int i = 0; i < statConclusionExportVos.size(); i++) {
            StatConclusionReportDTO vo = statConclusionExportVos.get(i);
            VisionScreeningResultReportDTO reportVo = new VisionScreeningResultReportDTO();
            BeanUtils.copyProperties(vo, reportVo);
            GlassesType glassesType = GlassesType.get(vo.getGlassesType());
            reportVo.setId(i + 1)
                    .setGenderDesc(GenderEnum.getName(vo.getGender()))
                    .setGlassesTypeDesc(Objects.isNull(glassesType) ? "--" : glassesType.desc);
            genScreeningData(vo, reportVo);
            reportVos.add(reportVo);
        }
        return reportVos;
    }

    /**
     * 组装初筛数据
     * @param vo
     * @param reportVo
     */
    private void genScreeningData(
            StatConclusionReportDTO vo, VisionScreeningResultReportDTO reportVo) {
        reportVo.setNakedVisions(
                        eyeDataFormat((BigDecimal) JSONPath.eval(
                                              vo, ScreeningResultPahtConst.RIGHTEYE_NAKED_VISION),
                                (BigDecimal) JSONPath.eval(
                                        vo, ScreeningResultPahtConst.LEFTEYE_NAKED_VISION),
                                1))
                .setCorrectedVisions(
                        eyeDataFormat((BigDecimal) JSONPath.eval(vo,
                                              ScreeningResultPahtConst.RIGHTEYE_CORRECTED_VISION),
                                (BigDecimal) JSONPath.eval(
                                        vo, ScreeningResultPahtConst.LEFTEYE_CORRECTED_VISION),
                                1))
                .setSphs(eyeDataFormat(
                        (BigDecimal) JSONPath.eval(vo, ScreeningResultPahtConst.RIGHTEYE_SPH),
                        (BigDecimal) JSONPath.eval(vo, ScreeningResultPahtConst.LEFTEYE_SPH), 2))
                .setCyls(eyeDataFormat(
                        (BigDecimal) JSONPath.eval(vo, ScreeningResultPahtConst.RIGHTEYE_CYL),
                        (BigDecimal) JSONPath.eval(vo, ScreeningResultPahtConst.LEFTEYE_CYL), 2))
                .setAxials(eyeDataFormat(
                        (BigDecimal) JSONPath.eval(vo, ScreeningResultPahtConst.RIGHTEYE_AXIAL),
                        (BigDecimal) JSONPath.eval(vo, ScreeningResultPahtConst.LEFTEYE_AXIAL), 0))
                .setSphericalEquivalents(
                        eyeDataFormat(StatUtil.getSphericalEquivalent(
                                              (BigDecimal) JSONPath.eval(
                                                      vo, ScreeningResultPahtConst.RIGHTEYE_SPH),
                                              (BigDecimal) JSONPath.eval(
                                                      vo, ScreeningResultPahtConst.RIGHTEYE_CYL)),
                                StatUtil.getSphericalEquivalent(
                                        (BigDecimal) JSONPath.eval(
                                                vo, ScreeningResultPahtConst.LEFTEYE_SPH),
                                        (BigDecimal) JSONPath.eval(
                                                vo, ScreeningResultPahtConst.LEFTEYE_CYL)),
                                2))
                .setLowVisionWarningLevel(vo.getNakedVisionWarningLevel())
                .setCorrectionType(vo.getVisionCorrection());
    }

    /**
     * 眼别数据格式化
     * @param rightEyeData
     * @param leftEyeData
     * @return
     */
    private String eyeDataFormat(BigDecimal rightEyeData, BigDecimal leftEyeData, int scale) {
        // 不足两位小数补0
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        switch (scale) {
            case 0:
                decimalFormat = new DecimalFormat("#");
                break;
            case 1:
                decimalFormat = new DecimalFormat("0.0");
        }
        return String.format("%s/%s",
                Objects.isNull(rightEyeData) ? "--" : decimalFormat.format(rightEyeData),
                Objects.isNull(leftEyeData) ? "--" : decimalFormat.format(leftEyeData));
    }

    /**
     * 获取区域学生计划筛查数量
     *
     * @param notificationId     通知ID
     * @param specificDistrictId 筛选区域ID
     * @return
     */
    private Integer getPlanScreeningStudentNum(int notificationId, Integer specificDistrictId) {
        Map<Integer, Long> planDistrictStudentMap =
                screeningPlanSchoolStudentService.getDistrictPlanStudentCountBySrcScreeningNoticeId(notificationId);
        List<Integer> validDistrictIds = districtService.getSpecificDistrictTreeAllDistrictIds(specificDistrictId);
        int planStudentNum = 0;
        for (Map.Entry<Integer, Long> entry : planDistrictStudentMap.entrySet()) {
            if (!validDistrictIds.contains(entry.getKey())) {
                continue;
            }
            planStudentNum += entry.getValue();
        }
        return planStudentNum;
    }
}

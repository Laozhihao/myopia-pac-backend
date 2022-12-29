package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONPath;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException;
import com.google.common.collect.Lists;
import com.wupol.framework.domain.ThreeTuple;
import com.wupol.framework.domain.TwoTuple;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.GlassesTypeEnum;
import com.wupol.myopia.business.api.management.domain.dto.*;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeItemsDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningResultPahtConst;
import com.wupol.myopia.business.core.screening.flow.domain.dto.*;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningNotice;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.*;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
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

    @Autowired
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    private static final String TABLE_LABEL_TOTAL = "total";
    private static final String TABLE_LABEL_ROW_TOTAL = "rowTotal";
    private static final String TABLE_LABEL_AVERAGE_VISION = "averageVision";
    private static final String TABLE_LABEL_TOTAL_RATIO = "totalRatio";
    private static final String TABLE_LABEL_TOTAL_STAT_LIST = "totalStatList";
    private static final String TOTAL_CHINESE = "合计";

    private static final String SORTED_LIST="sortedList";

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
    private TableBasicStatParams composeTableBasicParams(String name, long statNum, long totalStatNum) {
        return new TableBasicStatParams(name, convertToPercentage(statNum * 1f / totalStatNum), statNum, totalStatNum);
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
        double sumVisionL = statConclusions.stream().mapToDouble(sc->sc.getVisionL().doubleValue()).sum();
        double sumVisionR = statConclusions.stream().mapToDouble(sc->sc.getVisionR().doubleValue()).sum();
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
        List<StatConclusion> statConclusions = statConclusionService.listOfReleasePlanByQuery(query);
        if (CollectionUtil.isEmpty(statConclusions)) {
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
        schoolAgeMap.forEach((schoolAgeName,statConclusionList)-> schoolAgeWarningLevelTable.add(composeWarningLevelStat(schoolAgeName, statConclusionList)));
        schoolAgeWarningLevelTable.add(composeWarningLevelStat(TABLE_LABEL_TOTAL, validConclusions));
        return schoolAgeWarningLevelTable;
    }

    private List<Map<String, Object>> createSchoolAgeGenderVisionUnderCorrectedTable(List<StatConclusion> visionCorrectionConclusions, Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeGenderVisionUnderCorrectedTable = new ArrayList<>();
        schoolAgeMap.forEach((schoolAgeName,statConclusionList)->{
            List<StatConclusion> schoolAgeVisionCorrectionConclusions =
                    statConclusionList.stream()
                            .filter(x -> x.getVisionCorrection() != null && !VisionCorrection.NORMAL.code.equals(x.getVisionCorrection()))
                            .collect(Collectors.toList());
            schoolAgeGenderVisionUnderCorrectedTable.add(composeGenderVisionUnderCorrectedStat(schoolAgeName,schoolAgeVisionCorrectionConclusions));
        });

        schoolAgeGenderVisionUnderCorrectedTable.add(composeGenderVisionUnderCorrectedStat(TABLE_LABEL_TOTAL,
                visionCorrectionConclusions));
        return schoolAgeGenderVisionUnderCorrectedTable;
    }

    private List<Map<String, Object>> createSchoolAgeGenderVisionUncorrectedTable(List<StatConclusion> validConclusions, Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeGenderVisionUncorrectedTable = new ArrayList<>();
        schoolAgeMap.forEach((schoolAgeName,statConclusionList)->{
            List<StatConclusion> schoolAgeMyopiaConclusions =
                    statConclusionList.stream()
                            .filter(x -> x.getIsMyopia() != null && x.getIsMyopia())
                            .collect(Collectors.toList());
            schoolAgeGenderVisionUncorrectedTable.add(composeGenderVisionUncorrectedStat(schoolAgeName,schoolAgeMyopiaConclusions));
        });

        List<StatConclusion> myopiaConclusions =
                validConclusions.stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsMyopia())).collect(Collectors.toList());
        schoolAgeGenderVisionUncorrectedTable.add(composeGenderVisionUncorrectedStat(TABLE_LABEL_TOTAL,
                myopiaConclusions));
        return schoolAgeGenderVisionUncorrectedTable;
    }

    private List<Map<String, Object>> createSchoolAgeGenderTable(Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeGenderTable = new ArrayList<>();
        schoolAgeMap.forEach((schoolAgeName,statConclusionList)-> schoolAgeGenderTable.add(composeGenderStat(schoolAgeName, statConclusionList)));
        return schoolAgeGenderTable;
    }

    private List<Map<String, Object>> createSchoolAgeLowVisionLevelTable(List<StatConclusion> validConclusions,
                                                                         Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeLowVisionLevelTable = new ArrayList<>();
        schoolAgeMap.forEach((schoolAgeName,statConclusionList)-> schoolAgeLowVisionLevelTable.add(composeLowVisionLevelStat(schoolAgeName, statConclusionList)));
        schoolAgeLowVisionLevelTable.add(composeLowVisionLevelStat(TABLE_LABEL_TOTAL, validConclusions));
        return schoolAgeLowVisionLevelTable;
    }

    private List<Map<String, Object>> createSchoolAgeGenderLowVisionTable(List<StatConclusion> validConclusions,
                                                                          Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeGenderLowVisionTable = new ArrayList<>();
        schoolAgeMap.forEach((schoolAgeName,statConclusionList)-> schoolAgeGenderLowVisionTable.add(composeGenderLowVisionStat(schoolAgeName,statConclusionList)));
        schoolAgeGenderLowVisionTable.add(composeGenderLowVisionStat(TABLE_LABEL_TOTAL, validConclusions));
        return schoolAgeGenderLowVisionTable;
    }

    private List<Map<String, Object>> createSchoolAgeGenderMyopiaTable(List<StatConclusion> validConclusions,
                                                                       Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeGenderMyopiaTable = new ArrayList<>();
        schoolAgeMap.forEach((schoolAgeName,statConclusionList)-> schoolAgeGenderMyopiaTable.add(composeGenderMyopiaStat(schoolAgeName, statConclusionList)));
        schoolAgeGenderMyopiaTable.add(composeGenderMyopiaStat(TABLE_LABEL_TOTAL, validConclusions));
        return schoolAgeGenderMyopiaTable;
    }

    private List<Map<String, Object>> createSchoolAgeGlassesTypeTable(List<StatConclusion> validConclusions,
                                                                      Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeGlassesTypeTable = new ArrayList<>();
        schoolAgeMap.forEach((schoolAgeName,statConclusionList)-> schoolAgeGlassesTypeTable.add(composeGlassesTypeStat(schoolAgeName, statConclusionList)));
        schoolAgeGlassesTypeTable.add(composeGlassesTypeStat(TABLE_LABEL_TOTAL, validConclusions));
        return schoolAgeGlassesTypeTable;
    }

    private List<Map<String, Object>> createSchoolAgeMyopiaLevelTable(List<StatConclusion> validConclusions,
                                                                      Map<String, List<StatConclusion>> schoolAgeMap) {
        List<Map<String, Object>> schoolAgeMyopiaLevelTable = new ArrayList<>();
        schoolAgeMap.forEach((schoolAgeName,statConclusionList)-> schoolAgeMyopiaLevelTable.add(composeMyopiaLevelStat(schoolAgeName, statConclusionList)));
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
        conclusionDesc.put(SORTED_LIST, schoolAgeLowVisionRatio);
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
        List<BasicStatParams> sortedList = new ArrayList<>();
        for (Map<String, Object> item : schoolAgeGenderVisionCorrectionTable) {
            String schoolAgeName = (String) item.get("name");
            if (schoolAgeName.equals(TABLE_LABEL_TOTAL)) {
                continue;
            }
            SchoolAge schoolAge = SchoolAge.valueOf(schoolAgeName);
            List<TableBasicStatParams> list = (List<TableBasicStatParams>) item.get("list");
            TableBasicStatParams params = list.stream().filter(x -> x.getTitle().equals(TABLE_LABEL_TOTAL)).findFirst().orElseThrow(() -> new BusinessException("统计数据为空"));
            sortedList.add(new BasicStatParams(schoolAgeName, params.getRatio(), null));
            if (!SchoolAge.KINDERGARTEN.code.equals(schoolAge.code)) {
                primaryToHighTotalNum += params.getTotal();
                primaryToHighCorrectionNum += params.getNum();
            }
        }
        sortedList.sort(Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        Map<String, Object> conclusionDesc = new HashMap<>();
        conclusionDesc.put("totalList", totalList);
        conclusionDesc.put(SORTED_LIST, sortedList);
        conclusionDesc.put("primaryToHighTotalNum", primaryToHighTotalNum);
        conclusionDesc.put("primaryToHighCorrectionNum", primaryToHighCorrectionNum);
        conclusionDesc.put("primaryToHighCorrectionRatio", primaryToHighTotalNum == 0 ? 0 : convertToPercentage(primaryToHighCorrectionNum * 1f / primaryToHighTotalNum));
        conclusionDesc.put(title, schoolAgeGenderVisionCorrectionTable);
        return conclusionDesc;
    }

    /**
     * 构造 学龄/预警级别 描述
     * @param title 表格描述
     * @param schoolAgeWarningLevelTable 表格数据
     * @return
     */
    private Map<String, Object> composeSchoolAgeWarningLevelDesc(String title, List<Map<String, Object>> schoolAgeWarningLevelTable) {
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
                        break;
                    default:
                        break;
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
        long totalWarningNum = totalRowList.stream().mapToLong(BasicStatParams::getNum).sum();
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
        conclusionDesc.put(SORTED_LIST, schoolAgeMyopiaRatio);
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
            if (GlassesTypeEnum.NOT_WEARING.name().equals(params.getTitle())) {
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
        Map<String, Object> resultMap = new HashMap<>();
        float maleRatio = 0.00f;
        float femaleRatio = 0.00f;
        float totalRatio = 0.00f;
        if(CollectionUtil.isNotEmpty(list)){
            maleRatio =list.stream().filter(x -> GenderEnum.MALE.name().equals(x.getTitle())).map(BasicStatParams::getRatio).findFirst().orElse(0.00f);
            femaleRatio = list.stream().filter(x -> GenderEnum.FEMALE.name().equals(x.getTitle())).map(BasicStatParams::getRatio).findFirst().orElse(0.00f);
            totalRatio =list.stream().filter(x -> x.getTitle().equals(TABLE_LABEL_TOTAL)).map(BasicStatParams::getRatio).findFirst().orElse(0.00f);
        }
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
        long planStudentNum = screenPlanSchoolStudent.size();
        // 通过筛查数据进行统计
        StatConclusionQueryDTO query = new StatConclusionQueryDTO();
        query.setPlanId(planId);
        List<StatConclusion> statConclusions = statConclusionService.listByQuery(query);
        if (CollectionUtil.isEmpty(statConclusions)) {
            return null;
        }
        List<Integer> schoolIds = statConclusions.stream()
                .map(StatConclusion::getSchoolId)
                .distinct()
                .collect(Collectors.toList());
        // 将空学校也添加
        schoolIds.addAll(screeningPlanSchoolService.getByPlanIdNotInSchoolIds(planId, schoolIds));
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

        return StatWholeResultDTO.builder()
                .plan(sp)
                .schoolCount(schools.size())
                .planStudentNum(planStudentNum)
                .actualScreeningNum(actualScreeningNum)
                .validFirstScreeningNum(validFirstScreeningNum)
                .validRatio(convertToPercentage(validFirstScreeningNum * 1f / actualScreeningNum))
                .schoolAgeDistribution(businessSchoolAge.getSortedDistributionMap())
                .schoolExamples(schoolExamples)
                .schoolPersonnel(getSchoolPersonnel(schools, planSchoolStudentMap, schoolFirstScreenMap, schoolValidMap))
                .schoolAgePersonnel(getSchoolAgePersonnel(planSchoolAgeStudentMap, businessSchoolAge))
                .myopia(getMyopiaRatio(statBase.getValid()))
                .visionCorrection(getVisionCorrection(statBase.getValid()))
                .warnLevel(getWarnLevel(statBase.getValid()))
                .genderMyopia(getGenderMyopia(new StatGenderDTO(statBase.getValid())))
                .schoolAgeMyopia(getSchoolAgeMyopia(businessSchoolAge))
                .schoolMyopia(getSchoolMyopia(schools, schoolValidMap))
                .gradeMyopia(getGradeMyopia(statBase.getValid()))
                .build();
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
                .sorted(Comparator.comparing(StatSchoolPersonnelDTO::getActualScreeningNum).reversed()
                        .thenComparing(StatSchoolPersonnelDTO::getPlanScreeningNum,Comparator.reverseOrder()))
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
    private List<StatSchoolAgePersonnelDTO> getSchoolAgePersonnel(Map<Integer, Long> planSchoolAgeStudentMap, StatBusinessSchoolAgeDTO businessSchoolAge) {

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
        long myopiaNum = validConclusions.stream().filter(x -> Objects.equals(Boolean.TRUE,x.getIsMyopia()) || GlassesTypeEnum.ORTHOKERATOLOGY.code.equals(x.getGlassesType())).count();
        long lowVisionNum = validConclusions.stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsLowVision())).count();
        double vision = validConclusions.stream().map(x -> x.getVisionL().add(x.getVisionR())).mapToDouble(BigDecimal::doubleValue).sum();
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
        long uncorrectedNum = validConclusions.stream().filter(x -> com.wupol.myopia.business.common.utils.constant.VisionCorrection.UNCORRECTED.code.equals(x.getVisionCorrection())).count();
        long wearingNum = validConclusions.stream().filter(x -> x.getGlassesType() > 0).count();
        long underCorrectedNum = validConclusions.stream().filter(x -> com.wupol.myopia.business.common.utils.constant.VisionCorrection.UNDER_CORRECTED.code.equals(x.getVisionCorrection())).count();
        long enoughCorrectedNum = validConclusions.stream().filter(x -> com.wupol.myopia.business.common.utils.constant.VisionCorrection.ENOUGH_CORRECTED.code.equals(x.getVisionCorrection())).count();
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
        Map<Integer, Long> warningLevelMap = validConclusions.stream().filter(s->Objects.nonNull(s.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
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
        long maleMyopiaNum = statGenderDTO.getMale().stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsMyopia())).count();
        long femaleMyopiaNum = statGenderDTO.getFemale().stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsMyopia())).count();
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
        AtomicInteger allHighStatNum = new AtomicInteger();
        AtomicLong allHighSchoolNum = new AtomicLong();
        AtomicLong allHighMyopiaNum = new AtomicLong();
        // 判断是否有职高或普高
        AtomicBoolean haveAllHigh = new AtomicBoolean(false);
        List<MyopiaDTO> schoolAgeList = businessSchoolAge.getValidSchoolAgeNumMap().keySet().stream()
                .map(x -> {
                    List<StatConclusion> stat = businessSchoolAge.getValidSchoolAgeMap().getOrDefault(x, Collections.emptyList());
                    long myopiaNum = stat.stream().map(StatConclusion::getIsMyopia).filter(Objects::nonNull).count();
                    int statNum = stat.size();
                    Long schoolNum = businessSchoolAge.getValidSchoolAgeDistributionMap().getOrDefault(x, 0L);
                    Float ratio = convertToPercentage(myopiaNum * 1f / statNum);
                    if (x.equals(SchoolAge.HIGH.name()) || x.equals(SchoolAge.VOCATIONAL_HIGH.name())) {
                        haveAllHigh.getAndSet(true);
                        allHighStatNum.addAndGet(statNum);
                        allHighSchoolNum.addAndGet(schoolNum);
                        allHighMyopiaNum.addAndGet(myopiaNum);
                    }
                    return MyopiaDTO.getInstance(statNum, schoolNum, x, myopiaNum, ratio);
                }).collect(Collectors.toList());
        if (haveAllHigh.get()) {
            MyopiaDTO allHighMyopia = new MyopiaDTO();
            allHighMyopia.setStatNum(allHighStatNum.get());
            allHighMyopia.setSchoolNum(allHighSchoolNum.get());
            allHighMyopia.setKey("ALL_HIGH");
            allHighMyopia.setNum(allHighMyopiaNum);
            allHighMyopia.setRatio(convertToPercentage(allHighMyopiaNum.get() * 1f / allHighStatNum.get()));
            schoolAgeList.add(allHighMyopia);
        }
        return schoolAgeList;
    }

    /**
     * 获取学校近视信息
     * @param schools
     * @param schoolValidMap
     * @return
     */
    private List<MyopiaDTO> getSchoolMyopia(List<School> schools, Map<Integer, List<StatConclusion>> schoolValidMap) {
        List<MyopiaDTO> schoolMyopia = schools.stream()
                .map(x -> {
                    List<StatConclusion> stat = schoolValidMap.get(x.getId());
                    if (CollectionUtils.isEmpty(stat)) {
                        return MyopiaDTO.getInstance(0, x.getName(),
                                0, 0.00f);
                    }
                    long myopiaNum = stat.stream().map(StatConclusion::getIsMyopia).filter(Objects::nonNull).count();
                    return MyopiaDTO.getInstance(stat.size(), x.getName(),
                            myopiaNum, convertToPercentage(myopiaNum * 1f / stat.size()));
                })
                .sorted(Comparator.comparing(MyopiaDTO::getRatio).reversed()
                        .thenComparing(MyopiaDTO::getStatNum,Comparator.reverseOrder()))
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
                    long myopiaNum = stat.stream().map(StatConclusion::getIsMyopia).filter(Objects::nonNull).count();
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
        long warning0Num = validConclusions.stream().filter(x -> WarningLevel.ZERO.code.equals(x.getWarningLevel())).count();
        long warning1Num = validConclusions.stream().filter(x -> WarningLevel.ONE.code.equals(x.getWarningLevel())).count();
        long warning2Num = validConclusions.stream().filter(x -> WarningLevel.TWO.code.equals(x.getWarningLevel())).count();
        long warning3Num = validConclusions.stream().filter(x -> WarningLevel.THREE.code.equals(x.getWarningLevel())).count();
        long warningSPNum = validConclusions.stream().filter(x -> WarningLevel.ZERO_SP.code.equals(x.getWarningLevel())).count();
        long warningNum = warning0Num + warning1Num + warning2Num + warning3Num + warningSPNum;

        AverageVision averageVision = this.calculateAverageVision(validConclusions);
        float averageVisionValue =
                round2Digits((averageVision.getAverageVisionLeft() + averageVision.getAverageVisionRight()) / 2);

        // 通过遍历获取学校班级
        List<SchoolGradeItemsDTO> schoolGradeItems = schoolGradeService.getAllGradeList(schoolId);
        List<StatConclusionReportDTO> statConclusionReportDTOs =
                statConclusionService.getReportVo(srcScreeningNoticeId, planId, schoolId);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("schoolName", schoolName);
        resultMap.put("actualScreeningNum", totalFirstScreeningNum);
        resultMap.put("validFirstScreeningNum", validFirstScreeningNum);
        resultMap.put("maleNum", statGender.getMale().size());
        resultMap.put("femaleNum", statGender.getFemale().size());

        // 近视
        long myopiaNum = validConclusions.stream().filter(
                x -> Objects.nonNull(x.getMyopiaLevel())
                        && (MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code.equals(x.getMyopiaLevel())
                        || MyopiaLevelEnum.MYOPIA_LEVEL_MIDDLE.code.equals(x.getMyopiaLevel())
                        || MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code.equals(x.getMyopiaLevel()))).count();
        resultMap.put("myopiaRatio", convertToPercentage(myopiaNum * 1f / validFirstScreeningNum));

        // 视力低下
        long lowVisionNum = validConclusions.stream().filter(
                c -> Objects.nonNull(c.getNakedVisionWarningLevel())
                        && (WarningLevel.ONE.code.equals(c.getNakedVisionWarningLevel())
                        || WarningLevel.TWO.code.equals(c.getNakedVisionWarningLevel())
                        || WarningLevel.THREE.code.equals(c.getNakedVisionWarningLevel()))).count();
        resultMap.put("lowVisionRatio", convertToPercentage(lowVisionNum * 1f / validFirstScreeningNum));
        resultMap.put(TABLE_LABEL_AVERAGE_VISION, averageVisionValue);

        List<BasicStatParams> basicStatParamsList = new ArrayList<>();
        basicStatParamsList.add(new BasicStatParams("warningTotal",
                convertToPercentage(warningNum * 1f / validFirstScreeningNum),
                warningNum));
        basicStatParamsList.add(new BasicStatParams("warning0",
                convertToPercentage((warningSPNum + warning0Num) * 1f / validFirstScreeningNum),
                (warningSPNum +warning0Num)));
        basicStatParamsList.add(new BasicStatParams("warning1",
                convertToPercentage(warning1Num * 1f / validFirstScreeningNum),
                warning1Num));
        basicStatParamsList.add(new BasicStatParams("warning2",
                convertToPercentage(warning2Num * 1f / validFirstScreeningNum),
                warning2Num));
        basicStatParamsList.add(new BasicStatParams("warning3",
                convertToPercentage(warning3Num * 1f / validFirstScreeningNum),
                warning3Num));
        resultMap.put("warningLevelStat", basicStatParamsList);
        resultMap.put("genderLowVisionLevelDesc", composeGenderLowVisionLevelDesc(validConclusions));
        resultMap.put("schoolGradeLowVisionLevelDesc", composeSchoolGradeLowVisionLevelDesc(validConclusions, schoolGradeItems));
        resultMap.put("schoolGradeMyopiaLevelDesc", composeSchoolGradeMyopiaLevelDesc(validConclusions, schoolGradeItems));
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
    private List<Map<String, Object>> composeSchoolClassStudentStatList(List<SchoolGradeItemsDTO> schoolGradeItemList,
                                                                        List<StatConclusionReportDTO> statConclusionReportDTOs) {
        List<Map<String, Object>> schoolStudentStatList = new ArrayList<>();
        for (SchoolGradeItemsDTO schoolGradeItems : schoolGradeItemList) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeItems.getGradeCode());
            List<SchoolClassDTO> schoolClasses = schoolGradeItems.getChild();
            List<Map<String, Object>> schoolClassStatList = new ArrayList<>();
            for (SchoolClassDTO schoolClass : schoolClasses) {
                List<StatConclusionReportDTO> studentStatList =
                        statConclusionReportDTOs.stream()
                                .filter(x -> gradeCodeEnum.getCode().equals(x.getSchoolGradeCode())
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
     * 构建 年级 预警级别 统计
     *
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeSchoolGradeWarningLevelDesc(List<SchoolGradeItemsDTO> schoolGradeItemList,
                                                                   List<StatConclusion> statConclusions) {
        List<Map<String, Object>> schoolGradeGenderVisionTable = new ArrayList<>();
        for (SchoolGradeItemsDTO schoolGradeItems : schoolGradeItemList) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeItems.getGradeCode());
            List<StatConclusion> list = statConclusions.stream()
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
    private Map<String, Object> composeSchoolGradeGenderUnderCorrectedDesc(List<SchoolGradeItemsDTO> schoolGradeItemList,
                                                                           List<StatConclusion> statConclusions) {
        List<StatConclusion> myopiaConclusions = statConclusions
                .stream().filter(sc->Objects.equals(Boolean.TRUE,sc.getIsMyopia())).collect(Collectors.toList());
        List<Map<String, Object>> schoolGradeGenderVisionTable = new ArrayList<>();
        for (SchoolGradeItemsDTO schoolGradeItems : schoolGradeItemList) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeItems.getGradeCode());
            List<StatConclusion> list = myopiaConclusions
                    .stream().filter(x -> gradeCodeEnum.getCode().equals(x.getSchoolGradeCode())).collect(Collectors.toList());
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
                statConclusions.stream().filter(x -> x.getGlassesType() != null && !GlassesTypeEnum.NOT_WEARING.code.equals(x.getGlassesType())).count();
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
    private Map<String, Object> composeGenderLowVisionLevelDesc(List<StatConclusion> statConclusions) {
        List<StatConclusion> maleList = statConclusions.stream()
                .filter(x -> GenderEnum.MALE.type.equals(x.getGender()))
                .collect(Collectors.toList());
        List<StatConclusion> femaleList = statConclusions.stream()
                .filter(x -> GenderEnum.FEMALE.type.equals(x.getGender()))
                .collect(Collectors.toList());

        Map<String, Object> maleStat = composeLowVisionLevelStat(GenderEnum.MALE.name(), maleList);
        Map<String, Object> femaleStat = composeLowVisionLevelStat(GenderEnum.FEMALE.name(), femaleList);
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
        resultMap.put("topStat", getTopStatList(totalLevelStat.subList(1, totalSize - 1)));
        return resultMap;
    }

    /**
     * 构建 性别 近视 统计
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeGenderMyopiaLevelDesc(List<StatConclusion> statConclusions) {
        List<StatConclusion> maleList = statConclusions.stream()
                .filter(x -> GenderEnum.MALE.type.equals(x.getGender())).collect(Collectors.toList());
        List<StatConclusion> femaleList = statConclusions.stream()
                .filter(x -> GenderEnum.FEMALE.type.equals(x.getGender())).collect(Collectors.toList());

        Map<String, Object> maleStat = composeMyopiaLevelStat(GenderEnum.MALE.name(), maleList);
        Map<String, Object> femaleStat = composeMyopiaLevelStat(GenderEnum.FEMALE.name(), femaleList);
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
        resultMap.put("topStat", getTopStatList(totalLevelStat.subList(1, totalLevelStatSize - 1)));
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
        Optional<Map.Entry<Float, List<BasicStatParams>>> optional = map.entrySet().stream().min(Map.Entry.comparingByKey(Comparator.reverseOrder()));
        if (optional.isPresent()){
            return optional.get().getValue();
        }else {
            return new ArrayList<>();
        }
    }

    /**
     * 构建 年级 视力低下 统计
     *
     * @param statConclusions  筛查结果
     * @param schoolGradeItems 班级列表
     * @return Map<String, Object>
     */
    private Map<String, Object> composeSchoolGradeMyopiaLevelDesc(List<StatConclusion> statConclusions,
                                                                  List<SchoolGradeItemsDTO> schoolGradeItems) {
        List<BasicStatParams> schoolGradeMyopiaRatioList = new ArrayList<>();
        List<Map<String, Object>> list = new ArrayList<>();
        for (SchoolGradeItemsDTO gradeCode : schoolGradeItems) {
            Map<String, Object> lowVisionLevelStat = composeMyopiaLevelStat(GradeCodeEnum.getByName(gradeCode.getName()).getEnName(),
                    statConclusions.stream()
                            .filter(x -> gradeCode.getGradeCode().equals(x.getSchoolGradeCode()))
                            .collect(Collectors.toList()));
            list.add(lowVisionLevelStat);
            List<BasicStatParams> paramsList =
                    (List<BasicStatParams>) lowVisionLevelStat.get("list");
            schoolGradeMyopiaRatioList.add(new BasicStatParams(
                    GradeCodeEnum.getByName(gradeCode.getName()).getEnName(), paramsList.get(paramsList.size() - 1).getRatio(), null));
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
     * @param statConclusions  筛查结果
     * @param schoolGradeItems 班级列表
     * @return Map<String, Object>
     */
    private Map<String, Object> composeSchoolGradeLowVisionLevelDesc(List<StatConclusion> statConclusions,
                                                                     List<SchoolGradeItemsDTO> schoolGradeItems) {
        List<BasicStatParams> schoolGradeLowVisionRatioList = new ArrayList<>();
        List<Map<String, Object>> list = new ArrayList<>();
        for (SchoolGradeItemsDTO gradeCode : schoolGradeItems) {
            Map<String, Object> lowVisionLevelStat = composeLowVisionLevelStat(GradeCodeEnum.getByName(gradeCode.getName()).getEnName(),
                    statConclusions.stream()
                            .filter(x -> gradeCode.getGradeCode().equals(x.getSchoolGradeCode()))
                            .collect(Collectors.toList()));
            list.add(lowVisionLevelStat);
            List<BasicStatParams> paramsList =
                    (List<BasicStatParams>) lowVisionLevelStat.get("list");
            schoolGradeLowVisionRatioList.add(new BasicStatParams(
                    GradeCodeEnum.getByName(gradeCode.getName()).getEnName(), paramsList.get(paramsList.size() - 1).getRatio(), null));
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
            List<SchoolClassDTO> schoolClasses = schoolGradeItem.getChild();
            for (SchoolClassDTO schoolClass : schoolClasses) {
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

            List<SchoolClassDTO> schoolClasses = schoolGradeItem.getChild();
            for (SchoolClassDTO schoolClass : schoolClasses) {
                Map<String, Object> myopiaLevelStat = composeMyopiaLevelStat(schoolClass.getName(),
                        statConclusions.stream().filter(x ->
                                        schoolClass.getName().equals(x.getSchoolClassName())
                                                && gradeCode.getCode().equals(x.getSchoolGradeCode()))
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
        Predicate<StatConclusion> predicate = sc->Objects.equals(Boolean.TRUE,sc.getIsLowVision());
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
                x -> Objects.equals(Boolean.TRUE,x.getIsMyopia()) || GlassesTypeEnum.ORTHOKERATOLOGY.code.equals(x.getGlassesType());
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
    private Map<String, Object> composeGenderVisionUnderCorrectedStat(String name, List<StatConclusion> statConclusions) {
        Predicate<StatConclusion> predicate = x -> VisionCorrection.UNDER_CORRECTED.code.equals(x.getVisionCorrection());
        return composeGenderPredicateStat(name, statConclusions, predicate);
    }

    /**
     * 构建 学龄 性别 过滤条件 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @param predicate
     * @return
     */
    private Map<String, Object> composeGenderPredicateStat(String name, List<StatConclusion> statConclusions,
                                                           Predicate<StatConclusion> predicate) {
        long rowTotal = statConclusions.size();

        List<StatConclusion> maleList = statConclusions.stream().filter(x -> GenderEnum.MALE.type.equals(x.getGender())).collect(Collectors.toList());
        List<StatConclusion> femaleList = statConclusions.stream().filter(x -> GenderEnum.FEMALE.type.equals(x.getGender())).collect(Collectors.toList());

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
    private Map<String, Object> composeLowVisionLevelStat(String name,
                                                          List<StatConclusion> statConclusions) {
        Predicate<StatConclusion> levelOnePredicate = x -> WarningLevel.ONE.code.equals(x.getNakedVisionWarningLevel());
        Predicate<StatConclusion> levelTwoPredicate = x -> WarningLevel.TWO.code.equals(x.getNakedVisionWarningLevel());
        Predicate<StatConclusion> levelThreePredicate = x -> WarningLevel.THREE.code.equals(x.getNakedVisionWarningLevel());

        Map<String, Object> levelMap = composeLevelStat(name, statConclusions,
                levelOnePredicate, levelTwoPredicate, levelThreePredicate);
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
    private Map<String, Object> composeMyopiaLevelStat(String name,
                                                       List<StatConclusion> statConclusions) {
        Predicate<StatConclusion> levelEarlyPredicate =
                x -> MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.code.equals(x.getMyopiaLevel());
        Predicate<StatConclusion> levelOnePredicate =
                x -> MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code.equals(x.getMyopiaLevel());
        Predicate<StatConclusion> levelThreePredicate =
                x -> MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.code.equals(x.getMyopiaLevel());
        return composeMyopiaLevelStat(name, statConclusions, levelEarlyPredicate, levelOnePredicate,  levelThreePredicate);
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
                statConclusions.stream().filter(x -> WarningLevel.ZERO.code.equals(x.getWarningLevel()) || WarningLevel.ZERO_SP.code.equals(x.getWarningLevel())).count();
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
     * 构建 预警级别 统计
     *
     * @param name                标题
     * @param statConclusions     统计数据
     * @param levelOnePredicate   一级过滤条件
     * @param levelThreePredicate 三级过滤条件
     * @return
     */
    private Map<String, Object> composeMyopiaLevelStat(String name, List<StatConclusion> statConclusions,
                                                       Predicate<StatConclusion> levelEarlyPredicate,
                                                       Predicate<StatConclusion> levelOnePredicate,
                                                       Predicate<StatConclusion> levelThreePredicate) {
        long rowTotal = statConclusions.size();
        long levelEarlyNum = statConclusions.stream().filter(levelEarlyPredicate).count();
        long levelOneNum = statConclusions.stream().filter(levelOnePredicate).count();
        long levelThreeNum = statConclusions.stream().filter(levelThreePredicate).count();
        List<BasicStatParams> list = new ArrayList<>();
        list.add(composeBasicParams(MyopiaLevelEnum.MYOPIA_LEVEL_EARLY.desc, levelEarlyNum, rowTotal));
        list.add(composeBasicParams(MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.desc, levelOneNum, rowTotal));
        list.add(composeBasicParams(MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.desc, levelThreeNum, rowTotal));
        list.add(composeBasicParams("levelTotal", levelOneNum  + levelThreeNum, rowTotal));
        Map<String, Object> resultMap = new HashMap<>(2);
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
                statConclusions.stream().filter(x -> GlassesTypeEnum.FRAME_GLASSES.code.equals(x.getGlassesType())).count();
        long typeContactLensNum =
                statConclusions.stream().filter(x -> GlassesTypeEnum.CONTACT_LENS.code.equals(x.getGlassesType())).count();
        long typeOrthokeratologyNum =
                statConclusions.stream().filter(x -> GlassesTypeEnum.ORTHOKERATOLOGY.code.equals(x.getGlassesType())).count();
        long typeNotWearingNum =
                statConclusions.stream().filter(x -> GlassesTypeEnum.NOT_WEARING.code.equals(x.getGlassesType())).count();
        List<BasicStatParams> list = new ArrayList<>();
        list.add(composeBasicParams(GlassesTypeEnum.FRAME_GLASSES.name(), typeFrameNum, rowTotal));
        list.add(composeBasicParams(GlassesTypeEnum.FRAME_GLASSES.name(), typeContactLensNum, rowTotal));
        list.add(composeBasicParams(GlassesTypeEnum.ORTHOKERATOLOGY.name(), typeOrthokeratologyNum, rowTotal));
        list.add(composeBasicParams(GlassesTypeEnum.NOT_WEARING.name(), typeNotWearingNum, rowTotal));
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
            GlassesTypeEnum glassesType = GlassesTypeEnum.get(vo.getGlassesType());
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
                break;
            default:
                break;
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

    public VisualScreeningReportDTO getSchoolVisualReport(Integer planId, Integer schoolId) {

        ScreeningPlan sp = screeningPlanService.getById(planId);
        School school = schoolService.getById(schoolId);

        List<ScreeningPlanSchoolStudent> screenPlanSchoolStudent = screeningPlanSchoolStudentService.getByScreeningPlanIdAndSchoolId(planId, schoolId);
        // 过滤幼儿园
        List<ScreeningPlanSchoolStudent> validScreenPlanSchoolStudent = screenPlanSchoolStudent.stream().filter(student -> !SchoolAge.KINDERGARTEN.code.equals(student.getGradeType())).collect(Collectors.toList());
        // 获取按年级分数据,按班级分数据
        Map<Integer, List<ScreeningPlanSchoolStudent>> gradeStudentMap = validScreenPlanSchoolStudent.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getGradeId));
        Map<Integer, List<ScreeningPlanSchoolStudent>> classStudentMap = validScreenPlanSchoolStudent.stream().collect(Collectors.groupingBy(ScreeningPlanSchoolStudent::getClassId));

        // 预计需要筛查的学生数
        long planStudentNum = screenPlanSchoolStudent.size();
        // 通过筛查数据进行统计
        StatConclusionQueryDTO query = new StatConclusionQueryDTO();
        query.setPlanId(planId).setSchoolId(schoolId);
        List<StatConclusion> statConclusions = statConclusionService.listByQuery(query);
        if (CollectionUtil.isEmpty(statConclusions)) {
            return null;
        }
        // 过滤掉幼儿园数据
        List<StatConclusion> vaildStatConclusions = statConclusions.stream().filter(stat -> !SchoolAge.KINDERGARTEN.code.equals(stat.getSchoolAge())).collect(Collectors.toList());
        StatBaseDTO statBase = new StatBaseDTO(vaildStatConclusions);

        StatGenderDTO statGender = new StatGenderDTO(statBase.getValid());

        return new VisualScreeningReportDTO();
    }

    /**
     * 获取报告总述信息
     * @param sp
     * @param school
     * @param statBase
     * @param planScreeningNum
     * @return
     */
    public ScreeningSummaryDTO getScreeningSummary(ScreeningPlan sp, School school, StatBaseDTO statBase, StatGenderDTO statGender, int planScreeningNum, int gradeNum, int classNum) {

        // 按预警等级分类，计算预警人数
        List<StatConclusion> valid = statBase.getValid();
        int validSize = valid.size();
        Map<Integer, Long> warningLevelMap = valid.stream().filter(s->Objects.nonNull(s.getWarningLevel())).collect(Collectors.groupingBy(StatConclusion::getWarningLevel, Collectors.counting()));
        long warningNum = warningLevelMap.keySet().stream().filter(WarningLevel::isWarning).mapToLong(x -> warningLevelMap.getOrDefault(x, 0L)).sum();

        // 视力不良人数
        long lowVisionNum = valid.stream().filter(StatConclusion::getIsLowVision).count();
        // 近视人数
        long myopiaNum = valid.stream().filter(StatConclusion::getIsMyopia).count();

        return ScreeningSummaryDTO.builder()
                .schoolName(school.getName())
                .schoolDistrict(districtService.getDistrictName(school.getDistrictDetail()))
                .reportTime(new Date())
                .startTime(sp.getStartTime())
                .endTime(sp.getEndTime())
                .gradeNum(gradeNum)
                .classNum(classNum)
                .planScreeningNum(planScreeningNum)
                .unscreenedNum(planScreeningNum - statBase.getFirstScreen().size())
                .invalidScreeningNum(statBase.getFirstScreen().size() - validSize)
                .validScreeningNum(validSize)
                .maleValidScreeningNum(statGender.getMale().size())
                .femaleValidScreeningNum(statGender.getFemale().size())
                .averageVision(convertToPercentage(averageVision(valid).floatValue()))
                .lowVisionNum(lowVisionNum)
                .lowVisionRatio(convertToPercentage(lowVisionNum * 1.0f / validSize))
                .myopiaNum(myopiaNum)
                .myopiaRatio(convertToPercentage(myopiaNum * 1.0f / validSize))
                .uncorrectedRatio(convertToPercentage(valid.stream().filter(stat -> VisionCorrection.UNCORRECTED.code.equals(stat.getVisionCorrection())).count() * 1.0f / myopiaNum))
                .underCorrectedRatio(valid.stream().filter(stat -> VisionCorrection.UNDER_CORRECTED.code.equals(stat.getVisionCorrection())).count() * 1.0f /
                        valid.stream().filter(stat -> !GlassesTypeEnum.NOT_WEARING.code.equals(stat.getGlassesType())).count())
                .lightMyopiaRatio(convertToPercentage(valid.stream().filter(stat -> MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code.equals(stat.getMyopiaLevel())).count() * 1.0f / validSize))
                .highMyopiaRatio(convertToPercentage(valid.stream().filter(stat -> MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.code.equals(stat.getMyopiaLevel())).count() * 1.0f / validSize))
                .warningNum(warningNum)
                .warningRatio(convertToPercentage(warningNum * 1.0f / validSize))
                .warningLevelZeroNum(warningLevelMap.getOrDefault(WarningLevel.ZERO.code, 0L) + warningLevelMap.getOrDefault(WarningLevel.ZERO_SP.code, 0L))
                .warningLevelOneNum(warningLevelMap.getOrDefault(WarningLevel.ONE.code, 0L))
                .warningLevelTwoNum(warningLevelMap.getOrDefault(WarningLevel.TWO.code, 0L))
                .warningLevelThreeNum(warningLevelMap.getOrDefault(WarningLevel.THREE.code, 0L))
                .build();

    }

    /**
     * 平均视力
     * @param valid
     * @return
     */
    public BigDecimal averageVision(List<StatConclusion> valid) {
        // 去除夜戴角膜塑形镜的无法视力数据
        List<StatConclusion> hasVision = valid.stream().filter(stat -> !GlassesTypeEnum.ORTHOKERATOLOGY.code.equals(stat.getGlassesType())).collect(Collectors.toList());
        BigDecimal visionNum = hasVision.stream().map(stat -> stat.getVisionR().add(stat.getVisionL())).reduce(BigDecimal.ZERO, BigDecimal::add);
        return visionNum.divide(new BigDecimal(valid.size() * 2));
    }

    /**
     * 学生近视情况
     * @param statGender
     * @param summary
     * @param gradeCodes
     * @param statConclusionGradeMap
     * @param classMap
     * @param statConclusionClassMap
     * @return
     */
    public MyopiaInfoDTO getMyopiaInfo(StatGenderDTO statGender, ScreeningSummaryDTO summary,
                                       List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap,
                                       Map<String, List<SchoolClass>> classMap,
                                       Map<String, List<StatConclusion>> statConclusionClassMap) {
        ThreeTuple<List<MyopiaDTO>, Float, Float> genderMyopia = getGenderMyopia(statGender, summary);
        TwoTuple<List<MyopiaInfoDTO.StudentGenderMyopia>, List<SummaryDTO>> gradeMyopia = getGradeMyopia(gradeCodes, statConclusionGradeMap);
        return MyopiaInfoDTO.builder()
                .genderMyopia(genderMyopia.getFirst())
                .maleGeneralMyopiaRatio(genderMyopia.getSecond())
                .femaleGeneralMyopiaRatio(genderMyopia.getThird())
                .gradeMyopia(gradeMyopia.getFirst())
                .gradeMyopiaSummary(gradeMyopia.getSecond())
                .classMyopia(getClassMyopia(gradeCodes, classMap, statConclusionClassMap))
                .build();
    }

    /**
     * 获取不同性别学生近视情况
     * @param statGender
     * @param summary
     * @return 不同性别近视情况，男生占总体近视率，女生占总体近视率
     */
    public ThreeTuple<List<MyopiaDTO>, Float, Float> getGenderMyopia(StatGenderDTO statGender, ScreeningSummaryDTO summary) {

        int maleNum = statGender.getMale().size();
        int femaleNum = statGender.getFemale().size();

        long maleMyopiaNum = statGender.getMale().stream().filter(StatConclusion::getIsMyopia).count();
        long femaleMyopiaNum = statGender.getFemale().stream().filter(StatConclusion::getIsMyopia).count();

        MyopiaDTO male = MyopiaDTO.getInstance(maleNum, "男", maleMyopiaNum, convertToPercentage(maleMyopiaNum * 1.0f / maleNum));
        MyopiaDTO female = MyopiaDTO.getInstance(femaleNum, "女", femaleMyopiaNum, convertToPercentage(femaleMyopiaNum * 1.0f / femaleNum));
        MyopiaDTO total = MyopiaDTO.getInstance(summary.getValidScreeningNum(), "总体情况", summary.getMyopiaNum(), summary.getMyopiaRatio());

        return new ThreeTuple<>(Arrays.asList(male, female, total),
                convertToPercentage(maleMyopiaNum * 1.0f / summary.getValidScreeningNum()),
                convertToPercentage(femaleMyopiaNum * 1.0f / summary.getValidScreeningNum()));
    }

    /**
     * 获取学生近视监测结果（年级）及总结
     * @param gradeCodes
     * @param statConclusionGradeMap
     * @return  年级近视情况，年级近视总结
     */
    public TwoTuple<List<MyopiaInfoDTO.StudentGenderMyopia>, List<SummaryDTO>> getGradeMyopia(List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap) {
        // 整体年级各性别近视情况
        List<MyopiaInfoDTO.StudentGenderMyopia> gradeMyopia = gradeCodes.stream().map(grade -> {
            MyopiaInfoDTO.StudentGenderMyopia item = MyopiaInfoDTO.StudentGenderMyopia.getGradeInstance(GradeCodeEnum.getDesc(grade));
            conclusion2GenderMyopia(statConclusionGradeMap.get(grade), item);
            return item;
        }).collect(Collectors.toList());
        // 年级近视总结
        SummaryDTO general = gradeMyopiaSummary(gradeMyopia, MyopiaInfoDTO.StudentGenderMyopia::getMyopiaRatio, "general");
        // 男生近视总结
        SummaryDTO male = gradeMyopiaSummary(gradeMyopia, MyopiaInfoDTO.StudentGenderMyopia::getMaleMyopiaRatio, "male");
        // 女生近视总结
        SummaryDTO female = gradeMyopiaSummary(gradeMyopia, MyopiaInfoDTO.StudentGenderMyopia::getFemaleMyopiaRatio, "female");
        return TwoTuple.of(gradeMyopia, Arrays.asList(general, male, female));
    }

    /**
     * 年级近视总结
     * @param gradeMyopia
     * @param keyMapper
     * @param keyName
     * @return
     */
    private SummaryDTO gradeMyopiaSummary(List<MyopiaInfoDTO.StudentGenderMyopia> gradeMyopia, Function<? super MyopiaInfoDTO.StudentGenderMyopia, ? extends Float> keyMapper, String keyName) {
        TreeMap<Float, List<String>> summaryMap = gradeMyopia.stream()
                .collect(Collectors.toMap(keyMapper,
                        value -> Lists.newArrayList(value.getGradeName()),
                        (List<String> value1, List<String> value2) -> { value1.addAll(value2); return value1;},
                        TreeMap::new));
        return new SummaryDTO(keyName, summaryMap.lastEntry().getValue(), summaryMap.lastKey(), summaryMap.firstEntry().getValue(), summaryMap.firstKey());
    }

    /**
     * 获取学生近视监测结果（班级）
     * @param gradeCodes
     * @param classMap
     * @param statConclusionClassMap
     * @return
     */
    public List<MyopiaInfoDTO.StudentGenderMyopia> getClassMyopia(List<String> gradeCodes,
                                                                  Map<String, List<SchoolClass>> classMap,
                                                                  Map<String, List<StatConclusion>> statConclusionClassMap) {
        return gradeCodes.stream().map(grade -> {
            // 生成班级近视情况数据
            List<SchoolClass> classes = classMap.getOrDefault(grade, Collections.emptyList());
            List<MyopiaInfoDTO.StudentGenderMyopia> classMyopia = classes.stream().map(clazz -> {
                MyopiaInfoDTO.StudentGenderMyopia gradeMyopia = MyopiaInfoDTO.StudentGenderMyopia.getClassInstance(GradeCodeEnum.getDesc(grade), clazz.getName());
                conclusion2GenderMyopia(statConclusionClassMap.get(grade + clazz.getName()), gradeMyopia);
                return gradeMyopia;
            }).collect(Collectors.toList());
            // 每一条数据设置rowspan
            if (!CollectionUtils.isEmpty(classMyopia)) {
                classMyopia.get(0).setRowSpan(classMyopia.size());
            }
            return classMyopia;
        }).reduce(new ArrayList<>(), (list1, list2) -> {list1.addAll(list2); return list1;});
    }

    /**
     * 统计各性别近视情况
     * @param stats
     * @param genderMyopia
     * @return
     */
    private void conclusion2GenderMyopia(List<StatConclusion> stats, GenderMyopiaInfoDTO genderMyopia) {
        // 若没有统计数据，生成无数据情况下近视情况
        if (CollectionUtils.isEmpty(stats)) {
            genderMyopia.empty();
            return ;
        }
        // 统计按性别近视情况
        int validScreeningNum = stats.size();
        int myopiaNum = (int)stats.stream().filter(StatConclusion::getIsMyopia).count();
        StatGenderDTO statGender = new StatGenderDTO(stats);
        int maleNum = statGender.getMale().size();
        int maleMyopiaNum = (int)statGender.getMale().stream().filter(StatConclusion::getIsMyopia).count();
        int femaleNum = statGender.getFemale().size();
        int femaleMyopiaNum = (int)statGender.getFemale().stream().filter(StatConclusion::getIsMyopia).count();
        genderMyopia.generateData(validScreeningNum, myopiaNum, maleNum, maleMyopiaNum, femaleNum, femaleMyopiaNum);
    }

    /**
     * 学生近视情况
     * @param statGender
     * @param summary
     * @param gradeCodes
     * @param statConclusionGradeMap
     * @param classMap
     * @param statConclusionClassMap
     * @return
     */
    public VisionInfoDTO getVisionInfo(List<StatConclusion> valid, StatGenderDTO statGender, ScreeningSummaryDTO summary,
                                       List<String> gradeCodes,
                                       Map<String, List<StatConclusion>> statConclusionGradeMap,
                                       Map<String, List<SchoolClass>> classMap,
                                       Map<String, List<StatConclusion>> statConclusionClassMap) {
        ThreeTuple<MyopiaLevelDTO, MyopiaLevelDTO, VisionInfoDTO.LowVisionSummary> generalVision = getGeneralVision(valid);
        TwoTuple<List<VisionInfoDTO.GenderMyopiaLevel>, List<SummaryDTO>> genderVision = getGenderVision(statGender);
        TwoTuple<List<VisionInfoDTO.StudentMyopiaLevel>, SummaryDTO> gradeVision = getGradeVision(gradeCodes, statConclusionGradeMap);
        return VisionInfoDTO.builder()
                .general(generalVision.getFirst())
                .lowVision(generalVision.getSecond())
                .lowVisionSummary(generalVision.getThird())
                .genderVision(genderVision.getFirst())
                .genderVisionSummary(genderVision.getSecond())
                .gradeVision(gradeVision.getFirst())
                .gradeVisionSummary(gradeVision.getSecond())
                .classVision(getClassVision(gradeCodes, classMap, statConclusionClassMap))
                .build();
    }

    /**
     * 获取整体视力程度情况
     * @param valid
     * @return 整体视力程度情况, 视力不良程度情况, 视力不良总结
     */
    public ThreeTuple<MyopiaLevelDTO, MyopiaLevelDTO, VisionInfoDTO.LowVisionSummary> getGeneralVision(List<StatConclusion> valid) {
        // 整体视力程度情况
        MyopiaLevelDTO general = new MyopiaLevelDTO();
        conclusion2MyopiaLevel(valid, general, true);
        // 视力不良程度情况
        MyopiaLevelDTO lowVision = new MyopiaLevelDTO();
        conclusion2MyopiaLevel(valid, lowVision, false);
        // 视力不良总结
        VisionInfoDTO.LowVisionSummary lowVisionSummary = new VisionInfoDTO.LowVisionSummary();
        BeanUtils.copyProperties(getLowVisionSummary(general), lowVisionSummary);
        lowVisionSummary.setLowVisionRatio(Math.max(Math.max(lowVision.getLightMyopiaRatio(), lowVision.getMiddleMyopiaRatio()), lowVision.getHighMyopiaRatio()));
        return new ThreeTuple(general, lowVision, lowVisionSummary);
    }

    /**
     * 获取性别视力情况及总结
     * @param statGender
     * @return 性别视力情况，总结
     */
    public TwoTuple<List<VisionInfoDTO.GenderMyopiaLevel>, List<SummaryDTO>> getGenderVision(StatGenderDTO statGender) {
        // 男女视力情况
        VisionInfoDTO.GenderMyopiaLevel male = VisionInfoDTO.GenderMyopiaLevel.getInstance("男");
        conclusion2MyopiaLevel(statGender.getMale(), male, true);
        VisionInfoDTO.GenderMyopiaLevel female = VisionInfoDTO.GenderMyopiaLevel.getInstance("女");
        conclusion2MyopiaLevel(statGender.getFemale(), male,true);
        // 视力情况及总结
        return TwoTuple.of(Arrays.asList(male, female),  Arrays.asList(getLowVisionSummary(male), getLowVisionSummary(female)));
    }

    /**
     * 获取视力程度情况（年级）及总结
     * @param gradeCodes
     * @param statConclusionGradeMap
     * @return 视力程度情况（年级），总结
     */
    public TwoTuple<List<VisionInfoDTO.StudentMyopiaLevel>, SummaryDTO> getGradeVision(List<String> gradeCodes, Map<String, List<StatConclusion>> statConclusionGradeMap) {
        // 整体年级各性别视力情况
        List<VisionInfoDTO.StudentMyopiaLevel> gradeMyopia = gradeCodes.stream().map(grade -> {
            VisionInfoDTO.StudentMyopiaLevel item = VisionInfoDTO.StudentMyopiaLevel.getGradeInstance(GradeCodeEnum.getDesc(grade));
            conclusion2MyopiaLevel(statConclusionGradeMap.get(grade), item, true);
            return item;
        }).collect(Collectors.toList());
        // 年级整体视力不良总结
        SummaryDTO general = gradeVisionSummary(gradeMyopia, VisionInfoDTO.StudentMyopiaLevel::getLowVisionRatio, "general");
        return TwoTuple.of(gradeMyopia, general);
    }

    /**
     * 获取视力程度情况（班级）
     * @param gradeCodes
     * @param classMap
     * @param statConclusionClassMap
     * @return
     */
    public List<VisionInfoDTO.StudentMyopiaLevel> getClassVision(List<String> gradeCodes,
                                                                  Map<String, List<SchoolClass>> classMap,
                                                                  Map<String, List<StatConclusion>> statConclusionClassMap) {
        return gradeCodes.stream().map(grade -> {
            // 生成班级视力情况数据
            List<SchoolClass> classes = classMap.getOrDefault(grade, Collections.emptyList());
            List<VisionInfoDTO.StudentMyopiaLevel> classVision = classes.stream().map(clazz -> {
                VisionInfoDTO.StudentMyopiaLevel gradeVision = VisionInfoDTO.StudentMyopiaLevel.getClassInstance(GradeCodeEnum.getDesc(grade), clazz.getName());
                conclusion2MyopiaLevel(statConclusionClassMap.get(grade + clazz.getName()), gradeVision, true);
                return gradeVision;
            }).collect(Collectors.toList());
            // 每一条数据设置rowspan
            if (!CollectionUtils.isEmpty(classVision)) {
                classVision.get(0).setRowSpan(classVision.size());
            }
            return classVision;
        }).reduce(new ArrayList<>(), (list1, list2) -> {list1.addAll(list2); return list1;});
    }

    /**
     * 生成视力不良情况总结
     * @param general
     * @return
     */
    public SummaryDTO getLowVisionSummary(MyopiaLevelDTO general) {
        SummaryDTO summary = new SummaryDTO();
        // 设置最高占比
        summary.setHighRadio(Math.max(Math.max(general.getLightMyopiaRatio(), general.getMiddleMyopiaRatio()), general.getHighMyopiaRatio()));
        // 设置最高占比说明
        List<String> highLowVisionLevel = new ArrayList<>();
        if (summary.getHighRadio().equals(general.getLightMyopiaRatio())) {
            highLowVisionLevel.add("轻度视力不良率");
        }
        if (summary.getHighRadio().equals(general.getMiddleMyopiaRatio())) {
            highLowVisionLevel.add("中度视力不良率");
        }
        if (summary.getHighRadio().equals(general.getHighMyopiaRatio())) {
            highLowVisionLevel.add("高度视力不良率");
        }
        summary.setHighName(highLowVisionLevel);
        return summary;
    }

    /**
     * 年级近视总结
     * @param gradeVistel
     * @param keyMapper
     * @param keyName
     * @return
     */
    private SummaryDTO gradeVisionSummary(List<VisionInfoDTO.StudentMyopiaLevel> gradeVistel, Function<? super VisionInfoDTO.StudentMyopiaLevel, ? extends Float> keyMapper, String keyName) {
        TreeMap<Float, List<String>> summaryMap = gradeVistel.stream()
                .collect(Collectors.toMap(keyMapper,
                        value -> Lists.newArrayList(value.getGradeName()),
                        (List<String> value1, List<String> value2) -> { value1.addAll(value2); return value1;},
                        TreeMap::new));
        return new SummaryDTO(keyName, summaryMap.lastEntry().getValue(), summaryMap.lastKey(), summaryMap.firstEntry().getValue(), summaryMap.firstKey());
    }

    /**
     * 统计各性别近视情况
     * @param stats
     * @param myopiaLevel
     * @return
     */
    private void conclusion2MyopiaLevel(List<StatConclusion> stats, MyopiaLevelDTO myopiaLevel, boolean isGlobalRatio) {
        // 若没有统计数据，生成无数据情况下近视情况
        if (CollectionUtils.isEmpty(stats)) {
            myopiaLevel.empty();
            return ;
        }
        // 统计视力不良各级情况
        Map<Integer, Long> lowVisionLevelMap = stats.stream().collect(Collectors.groupingBy(StatConclusion::getLowVisionLevel, Collectors.counting()));
        int validScreeningNum = stats.size();
        int lowVisionNum = (int)stats.stream().filter(StatConclusion::getIsLowVision).count();
        int lightMyopiaNum = lowVisionLevelMap.getOrDefault(LowVisionLevelEnum.LOW_VISION_LEVEL_LIGHT.code, 0L).intValue();
        int middleMyopiaNum = lowVisionLevelMap.getOrDefault(LowVisionLevelEnum.LOW_VISION_LEVEL_MIDDLE.code, 0L).intValue();
        int highMyopiaNum = lowVisionLevelMap.getOrDefault(LowVisionLevelEnum.LOW_VISION_LEVEL_HIGH.code, 0L).intValue();
        myopiaLevel.generateData(validScreeningNum, lowVisionNum, lightMyopiaNum, middleMyopiaNum, highMyopiaNum, isGlobalRatio);
    }

}

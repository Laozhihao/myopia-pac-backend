package com.wupol.myopia.business.management.service;

import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.constant.GlassesType;
import com.wupol.myopia.business.management.constant.GenderEnum;
import com.wupol.myopia.business.management.constant.GradeCodeEnum;
import com.wupol.myopia.business.management.constant.SchoolAge;
import com.wupol.myopia.business.management.constant.VisionCorrection;
import com.wupol.myopia.business.management.constant.WarningLevel;
import com.wupol.myopia.business.management.domain.dto.stat.BasicStatParams;
import com.wupol.myopia.business.management.domain.dto.stat.TableBasicStatParams;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.StatConclusion;
import com.wupol.myopia.business.management.domain.query.StatConclusionQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;

@Service
public class StatReportService {
    @Autowired
    private StatConclusionService statConclusionService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private GovDeptService govDeptService;

    @Autowired
    private ScreeningPlanService screeningPlanService;

    @Autowired
    private ScreeningNoticeService screeningNoticeService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    /**
     * 构建区域及其下级所有符合当前用户范围的区域的搜索条件
     * @param districtId 区域 id
     * @param currentUser 当前用户
     * @return
     * @throws IOException
     */
    private StatConclusionQuery composeDistrictQuery(Integer districtId, CurrentUser currentUser)
            throws IOException {
        StatConclusionQuery query = new StatConclusionQuery();
        List<Integer> userDistrictIds = getCurrentUserDistrictIds(currentUser);
        if (districtId != null && districtId >= 0) {
            List<District> districts =
                    districtService.getChildDistrictByParentIdPriorityCache(districtId);
            List<Integer> selectDistrictIds =
                    districts.stream().map(District::getId).collect(Collectors.toList());
            selectDistrictIds.add(districtId);
            if (userDistrictIds != null) {
                selectDistrictIds.retainAll(userDistrictIds);
            }
            query.setDistrictIds(selectDistrictIds);
        } else {
            query.setDistrictIds(userDistrictIds);
        }
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
        Long maleNum =
                statConclusions.stream().filter(x -> x.getGender() == GenderEnum.MALE.type).count();
        Long femaleNum = total - maleNum;
        return new HashMap<String, Object>() {
            {
                put("name", name);
                put("male", maleNum);
                put("female", femaleNum);
                put("total", total);
            }
        };
    }

    /**
     * 获取当前用户所有权限的区域ID
     * @return
     * @throws IOException
     */
    private List<Integer> getCurrentUserDistrictIds(CurrentUser currentUser) throws IOException {
        if (currentUser.isPlatformAdminUser() || currentUser.getOrgId() == null) {
            return null;
        }
        GovDept govDept = govDeptService.getById(currentUser.getOrgId());
        District userDistrict = districtService.getById(govDept.getDistrictId());
        List<District> districts =
                districtService.getChildDistrictByParentIdPriorityCache(userDistrict.getId());
        districts.add(userDistrict);
        return districts.stream().map(District::getId).collect(Collectors.toList());
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
    private Float round2Digits(Double num) {
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
        double sumVisionL = statConclusions.stream().mapToDouble(x -> x.getVisionL()).sum();
        double sumVisionR = statConclusions.stream().mapToDouble(x -> x.getVisionR()).sum();
        float avgVisionL = round2Digits(sumVisionL / size);
        float avgVisionR = round2Digits(sumVisionR / size);
        return AverageVision.builder()
                .averageVisionLeft(avgVisionL)
                .averageVisionRight(avgVisionR)
                .build();
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
     * @param srcScreeningNoticeId 政府下发的原始通知id
     * @param districtId 区域id
     * @return
     * @throws IOException
     */
    public Map getDistrictStatData(int srcScreeningNoticeId, int districtId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        StatConclusionQuery query = composeDistrictQuery(districtId, currentUser);
        query.setSrcScreeningNoticeId(srcScreeningNoticeId);
        List<StatConclusion> statConclusions = statConclusionService.listByQuery(query);
        if (statConclusions == null || statConclusions.size() == 0) {
            return null;
        }
        District district = districtService.getById(districtId);
        String districtName = district.getName();
        int planScreeningNum =
                screeningPlanService.getScreeningPlanStudentNum(srcScreeningNoticeId, currentUser);
        ScreeningNotice notice = screeningNoticeService.getById(srcScreeningNoticeId);
        Date startDate = notice.getStartTime();
        Date endDate = notice.getEndTime();
        List<StatConclusion> firstScreenConclusions =
                statConclusions.stream()
                        .filter(x -> x.getIsRescreen() == false)
                        .collect(Collectors.toList());
        long totalFirstScreeningNum = firstScreenConclusions.size();
        List<Integer> schoolIds = statConclusions.stream()
                                          .map(x -> x.getSchoolId())
                                          .distinct()
                                          .collect(Collectors.toList());
        List<School> schools = schoolService.getByIds(schoolIds);
        List<String> schoolExamples =
                schools.stream().map(School::getName).limit(3).collect(Collectors.toList());
        List<StatConclusion> validConclusions = firstScreenConclusions.stream()
                                                        .filter(x -> x.getIsValid() == true)
                                                        .collect(Collectors.toList());
        List<StatConclusion> visionCorrectionConclusions =
                firstScreenConclusions.stream()
                        .filter(x -> x.getVisionCorrection() != VisionCorrection.NORMAL.code)
                        .collect(Collectors.toList());

        long validFirstScreeningNum = validConclusions.size();

        List<StatConclusion> maleList = validConclusions.stream()
                                                .filter(x -> x.getGender() == GenderEnum.MALE.type)
                                                .collect(Collectors.toList());
        List<StatConclusion> femaleList =
                validConclusions.stream()
                        .filter(x -> x.getGender() == GenderEnum.FEMALE.type)
                        .collect(Collectors.toList());
        List<StatConclusion> kindergartenList =
                validConclusions.stream()
                        .filter(x -> x.getSchoolAge() == SchoolAge.KINDERGARTEN.code)
                        .collect(Collectors.toList());
        List<StatConclusion> primaryList =
                validConclusions.stream()
                        .filter(x -> x.getSchoolAge() == SchoolAge.PRIMARY.code)
                        .collect(Collectors.toList());
        List<StatConclusion> juniorList =
                validConclusions.stream()
                        .filter(x -> x.getSchoolAge() == SchoolAge.JUNIOR.code)
                        .collect(Collectors.toList());
        List<StatConclusion> highList =
                validConclusions.stream()
                        .filter(x -> x.getSchoolAge() == SchoolAge.HIGH.code)
                        .collect(Collectors.toList());
        List<StatConclusion> vocationalHighList =
                validConclusions.stream()
                        .filter(x -> x.getSchoolAge() == SchoolAge.VOCATIONAL_HIGH.code)
                        .collect(Collectors.toList());

        Map<String, List<StatConclusion>> schoolAgeMap =
                new HashMap<String, List<StatConclusion>>() {
                    {
                        put(SchoolAge.KINDERGARTEN.name(), kindergartenList);
                        put(SchoolAge.PRIMARY.name(), primaryList);
                        put(SchoolAge.JUNIOR.name(), juniorList);
                        put(SchoolAge.HIGH.name(), highList);
                        put(SchoolAge.VOCATIONAL_HIGH.name(), vocationalHighList);
                    }
                };

        List<StatConclusion> myopiaConclusions =
                validConclusions.stream().filter(x -> x.getIsMyopia()).collect(Collectors.toList());

        List<Map<String, Object>> schoolGenderTable = new ArrayList<>();
        for (School school : schools) {
            int schoolId = school.getId();
            List<StatConclusion> schoolList = validConclusions.stream()
                                                      .filter(x -> x.getSchoolId() == schoolId)
                                                      .collect(Collectors.toList());
            Map<String, Object> schoolGenderStat = composeGenderStat(school.getName(), schoolList);
            schoolGenderTable.add(schoolGenderStat);
        };

        List<Map<String, Object>> schoolAgeGenderTable = new ArrayList<Map<String, Object>>() {
            {
                for (String schoolAgeName : schoolAgeMap.keySet()) {
                    add(composeGenderStat(schoolAgeName, schoolAgeMap.get(schoolAgeName)));
                }
            }
        };

        List<Map<String, Object>> schoolAgeLowVisionLevelTable =
                new ArrayList<Map<String, Object>>() {
                    {
                        for (String schoolAgeName : schoolAgeMap.keySet()) {
                            add(composeLowVisionLevelStat(
                                    schoolAgeName, schoolAgeMap.get(schoolAgeName)));
                        }
                        add(composeLowVisionLevelStat("total", validConclusions));
                    }
                };

        List<Map<String, Object>> schoolAgeGenderLowVisionTable =
                new ArrayList<Map<String, Object>>() {
                    {
                        for (String schoolAgeName : schoolAgeMap.keySet()) {
                            add(composeGenderLowVisionStat(
                                    schoolAgeName, schoolAgeMap.get(schoolAgeName)));
                        }
                        add(composeGenderLowVisionStat("total", validConclusions));
                    }
                };

        List<Map<String, Object>> schoolAgeGenderMyopiaTable =
                new ArrayList<Map<String, Object>>() {
                    {
                        for (String schoolAgeName : schoolAgeMap.keySet()) {
                            add(composeGenderMyopiaStat(
                                    schoolAgeName, schoolAgeMap.get(schoolAgeName)));
                        }
                        add(composeGenderMyopiaStat("total", validConclusions));
                    }
                };

        List<Map<String, Object>> schoolGradeGenderMyopiaTable =
                new ArrayList<Map<String, Object>>() {
                    {
                        for (GradeCodeEnum gradeCode : GradeCodeEnum.values()) {
                            if (gradeCode.equals(GradeCodeEnum.OTHER)) continue;
                            add(composeGenderMyopiaStat(gradeCode.name(),
                                    validConclusions.stream()
                                            .filter(x
                                                    -> x.getSchoolGradeCode().equals(
                                                            gradeCode.getCode()))
                                            .collect(Collectors.toList())));
                        }
                        add(composeGenderMyopiaStat("total", validConclusions));
                    }
                };

        List<Map<String, Object>> schoolAgeMyopiaLevelTable = new ArrayList<Map<String, Object>>() {
            {
                for (String schoolAgeName : schoolAgeMap.keySet()) {
                    add(composeMyopiaLevelStat(schoolAgeName, schoolAgeMap.get(schoolAgeName)));
                }
                add(composeMyopiaLevelStat("total", validConclusions));
            }
        };

        List<Map<String, Object>> schoolAgeGlassesTypeTable = new ArrayList<Map<String, Object>>() {
            {
                for (String schoolAgeName : schoolAgeMap.keySet()) {
                    add(composeGlassesTypeStat(schoolAgeName, schoolAgeMap.get(schoolAgeName)));
                }
                add(composeGlassesTypeStat("total", validConclusions));
            }
        };

        List<Map<String, Object>> schoolAgeGenderVisionUncorrectedTable =
                new ArrayList<Map<String, Object>>() {
                    {
                        for (String schoolAgeName : schoolAgeMap.keySet()) {
                            List<StatConclusion> schoolAgeMyopiaConclusions =
                                    schoolAgeMap.get(schoolAgeName)
                                            .stream()
                                            .filter(x -> x.getIsMyopia())
                                            .collect(Collectors.toList());
                            add(composeSchoolGenderVisionUncorrectedStat(
                                    schoolAgeName, schoolAgeMyopiaConclusions));
                        }
                        add(composeSchoolGenderVisionUncorrectedStat("total", myopiaConclusions));
                    }
                };
        List<Map<String, Object>> schoolAgeGenderVisionUnderCorrectedTable =
                new ArrayList<Map<String, Object>>() {
                    {
                        for (String schoolAgeName : schoolAgeMap.keySet()) {
                            List<StatConclusion> schoolAgeVisionCorrectionConclusions =
                                    schoolAgeMap.get(schoolAgeName)
                                            .stream()
                                            .filter(x
                                                    -> x.getVisionCorrection()
                                                            != VisionCorrection.NORMAL.code)
                                            .collect(Collectors.toList());
                            add(composeSchoolGenderVisionUnderCorrectedStat(
                                    schoolAgeName, schoolAgeVisionCorrectionConclusions));
                        }
                        add(composeSchoolGenderVisionUnderCorrectedStat(
                                "total", visionCorrectionConclusions));
                    }
                };

        List<Map<String, Object>> schoolAgeWarningLevelTable =
                new ArrayList<Map<String, Object>>() {
                    {
                        for (String schoolAgeName : schoolAgeMap.keySet()) {
                            add(composeSchoolAgeWarningLevelStat(
                                    schoolAgeName, schoolAgeMap.get(schoolAgeName)));
                        }
                        add(composeSchoolAgeWarningLevelStat("total", validConclusions));
                    }
                };
        return new HashMap<String, Object>() {
            {
                put("districtName", districtName);
                if (startDate != null) {
                    put("startDate", startDate.getTime());
                }
                if (endDate != null) {
                    put("endDate", endDate.getTime());
                }
                put("schoolExamples", schoolExamples);
                put("planScreeningNum", planScreeningNum);
                put("actualScreeningNum", totalFirstScreeningNum);
                put("validFirstScreeningNum", validFirstScreeningNum);
                put("kindergartenScreeningNum", kindergartenList.size());
                put("primaryScreeningNum", primaryList.size());
                put("juniorScreeningNum", juniorList.size());
                put("highScreeningNum", highList.size());
                put("maleNum", maleList.size());
                put("femaleNum", femaleList.size());
                put("vocationalHighScreeningNum", vocationalHighList.size());
                put("schoolGenderTable", schoolGenderTable);
                put("schoolAgeGenderTable", schoolAgeGenderTable);
                put("schoolAgeLowVisionLevelDesc",
                        composeSchoolAgeLowVisionLevelDesc(schoolAgeLowVisionLevelTable));
                put("schoolAgeGenderLowVisionDesc",
                        composeSchoolAgeGenderVisionDesc(
                                "schoolAgeGenderLowVisionTable", schoolAgeGenderLowVisionTable));
                put("schoolAgeGenderMyopiaDesc",
                        composeSchoolAgeGenderVisionDesc(
                                "schoolAgeGenderMyopiaTable", schoolAgeGenderMyopiaTable));
                put("schoolGradeGenderMyopiaTable", schoolGradeGenderMyopiaTable);
                put("schoolAgeMyopiaLevelDesc",
                        composeSchoolAgeMyopiaLevelDesc(
                                "schoolAgeMyopiaLevelTable", schoolAgeMyopiaLevelTable));
                put("schoolAgeGlassesTypeTable", schoolAgeGlassesTypeTable);
                put("schoolAgeGenderVisionUncorrectedTable", schoolAgeGenderVisionUncorrectedTable);
                put("schoolAgeGenderVisionUnderCorrectedTable",
                        schoolAgeGenderVisionUnderCorrectedTable);
                put("schoolAgeWarningLevelTable", schoolAgeWarningLevelTable);
            }
        };
    }

    /**
     * 构造 学龄/视力低下程度 描述
     * @param schoolAgeLowVisionLevelTable
     * @return
     */
    private Map<String, Object> composeSchoolAgeLowVisionLevelDesc(
            List<Map<String, Object>> schoolAgeLowVisionLevelTable) {
        int size = schoolAgeLowVisionLevelTable.size();
        Map<String, Object> conclusionDesc = new HashMap<>();
        List<BasicStatParams> schoolAgeLowVisionRatio = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Map<String, Object> stats = schoolAgeLowVisionLevelTable.get(i);
            List<BasicStatParams> list = (List<BasicStatParams>) stats.get("list");
            Float ratio = (Float) list.get(list.size() - 1).getRatio();
            if (i != size - 1) {
                String name = (String) stats.get("name");
                schoolAgeLowVisionRatio.add(new BasicStatParams(name, ratio, null));
            } else {
                conclusionDesc.put("totalAverageVision", stats.get("averageVision"));
                conclusionDesc.put("totalLowVisionRatio", ratio);
            }
        }
        Collections.sort(schoolAgeLowVisionRatio,
                Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        conclusionDesc.put("sortedList", schoolAgeLowVisionRatio);
        conclusionDesc.put("schoolAgeLowVisionLevelTable", schoolAgeLowVisionLevelTable);
        return conclusionDesc;
    }

    /**
     * 构造 学龄/性别/视力情况 描述
     * @param schoolAgeGenderLowVisionTable
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
     * 构造 学龄/视力情况 描述
     * @param schoolAgeGenderLowVisionTable
     * @return
     */
    private Map<String, Object> composeSchoolAgeMyopiaLevelDesc(
            String title, List<Map<String, Object>> schoolAgeMyopiaLevelTable) {
        int size = schoolAgeMyopiaLevelTable.size();
        List<BasicStatParams> schoolAgeMyopiaRatio = new ArrayList<>();
        Map<String, Object> conclusionDesc = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Map<String, Object> stats = schoolAgeMyopiaLevelTable.get(i);
            List<BasicStatParams> list = (List<BasicStatParams>) stats.get("list");
            Float ratio = (Float) list.get(list.size() - 1).getRatio();
            if (i != size - 1) {
                String name = (String) stats.get("name");
                schoolAgeMyopiaRatio.add(new BasicStatParams(name, ratio, null));
            }
        }
        Collections.sort(schoolAgeMyopiaRatio,
                Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        conclusionDesc.put("sortedList", schoolAgeMyopiaRatio);
        conclusionDesc.put(title, schoolAgeMyopiaLevelTable);
        return conclusionDesc;
    }

    /**
     * 构造 性别 占比
     * @param list
     * @return
     */
    private Map<String, Object> composeGenderRatio(List<TableBasicStatParams> list) {
        float maleRatio = list.stream()
                                  .filter(x -> x.getTitle().equals(GenderEnum.MALE.name()))
                                  .map(x -> x.getRatio())
                                  .findFirst()
                                  .get();
        float femaleRatio = list.stream()
                                    .filter(x -> x.getTitle().equals(GenderEnum.FEMALE.name()))
                                    .map(x -> x.getRatio())
                                    .findFirst()
                                    .get();
        float totalRatio = list.stream()
                                   .filter(x -> x.getTitle().equals("total"))
                                   .map(x -> x.getRatio())
                                   .findFirst()
                                   .get();
        return new HashMap() {
            {
                put("maleRatio", maleRatio);
                put("femaleRatio", femaleRatio);
                put("totalRatio", totalRatio);
            }
        };
    }

    /**
     * 获取学校筛查报告数据
     * @param srcScreeningNoticeId
     * @param schoolId
     * @return
     * @throws IOException
     */
    public Map<String, Object> getSchoolStatData(int srcScreeningNoticeId, int schoolId)
            throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<Integer> districtIds = this.getCurrentUserDistrictIds(currentUser);
        StatConclusionQuery query = new StatConclusionQuery();
        query.setDistrictIds(districtIds);
        query.setSrcScreeningNoticeId(srcScreeningNoticeId);
        query.setSchoolId(schoolId);
        List<StatConclusion> statConclusions = statConclusionService.listByQuery(query);
        if (statConclusions == null) {
            return null;
        }

        Integer planStudentNum = screeningPlanSchoolStudentService.countPlanSchoolStudent(
                srcScreeningNoticeId, schoolId);

        // Notice notice = noticeService.getById(srcScreeningNoticeId);
        // Date startDate = notice.getStartTime();
        // Date endDate = notice.getEndTime();
        // List<StatConclusion> firstScreenConclusions =
        //         statConclusions.stream()
        //                 .filter(x -> x.getIsRescreen() == false)
        //                 .collect(Collectors.toList());
        // long totalFirstScreeningNum = firstScreenConclusions.size();
        // List<Integer> schoolIds = statConclusions.stream()
        //                                   .map(x -> x.getSchoolId())
        //                                   .distinct()
        //                                   .collect(Collectors.toList());

        // List<StatConclusion> validConclusions = firstScreenConclusions.stream()
        //                                                 .filter(x -> x.getIsValid() == true)
        //                                                 .collect(Collectors.toList());
        School school = schoolService.getById(schoolId);
        String schoolName = school.getName();
        return new HashMap<String, Object>() {
            {
                put("schoolName", schoolName);
                put("planStudentNum", planStudentNum);
            }
        };
    }

    /**
     * 构建 性别 视力低下 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeGenderLowVisionStat(
            String name, List<StatConclusion> statConclusions) {
        Predicate<StatConclusion> predicate = x -> x.getIsLowVision();
        return composeSchoolAgeGenderPredicateStat(name, statConclusions, predicate);
    }

    /**
     * 构建 性别 近视 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeGenderMyopiaStat(
            String name, List<StatConclusion> statConclusions) {
        Predicate<StatConclusion> predicate = x -> x.getIsMyopia();
        return composeSchoolAgeGenderPredicateStat(name, statConclusions, predicate);
    }

    /**
     * 构建 学龄 视力未矫 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeSchoolGenderVisionUncorrectedStat(
            String name, List<StatConclusion> statConclusions) {
        Predicate<StatConclusion> predicate =
                x -> x.getVisionCorrection() == VisionCorrection.UNCORRECTED.code;
        return composeSchoolAgeGenderPredicateStat(name, statConclusions, predicate);
    }

    /**
     * 构建 学龄 视力欠矫 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeSchoolGenderVisionUnderCorrectedStat(
            String name, List<StatConclusion> statConclusions) {
        Predicate<StatConclusion> predicate =
                x -> x.getVisionCorrection() == VisionCorrection.UNDER_CORRECTED.code;
        return composeSchoolAgeGenderPredicateStat(name, statConclusions, predicate);
    }

    /**
     * 构建 学龄 性别 过滤条件 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @param predicate
     * @return
     */
    private Map<String, Object> composeSchoolAgeGenderPredicateStat(String name,
            List<StatConclusion> statConclusions, Predicate<StatConclusion> predicate) {
        long rowTotal = statConclusions.size();

        List<StatConclusion> maleList = statConclusions.stream()
                                                .filter(x -> x.getGender() == GenderEnum.MALE.type)
                                                .collect(Collectors.toList());

        List<StatConclusion> femaleList =
                statConclusions.stream()
                        .filter(x -> x.getGender() == GenderEnum.FEMALE.type)
                        .collect(Collectors.toList());

        Long maleLowVisionNum = maleList.stream().filter(predicate).count();
        Long femaleLowVisionNum = femaleList.stream().filter(predicate).count();
        List<TableBasicStatParams> list = new ArrayList<TableBasicStatParams>() {
            {
                add(composeTableBasicParams(
                        GenderEnum.MALE.name(), maleLowVisionNum, maleList.size()));
                add(composeTableBasicParams(
                        GenderEnum.FEMALE.name(), femaleLowVisionNum, femaleList.size()));
                add(composeTableBasicParams(
                        "total", maleLowVisionNum + femaleLowVisionNum, rowTotal));
            }
        };
        return new HashMap<String, Object>() {
            {
                put("name", name);
                put("rowTotal", rowTotal);
                put("list", list);
            }
        };
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
                x -> x.getNakedVisionWarningLevel() == WarningLevel.ONE.code;
        Predicate<StatConclusion> levelTwoPredicate =
                x -> x.getNakedVisionWarningLevel() == WarningLevel.TWO.code;
        Predicate<StatConclusion> levelThreePredicate =
                x -> x.getNakedVisionWarningLevel() == WarningLevel.THREE.code;
        Map<String, Object> levelMap = composeLevelStat(
                name, statConclusions, levelOnePredicate, levelTwoPredicate, levelThreePredicate);
        AverageVision averageVision = this.calculateAverageVision(statConclusions);
        float averageVisionValue =
                (averageVision.getAverageVisionLeft() + averageVision.getAverageVisionRight()) / 2;
        levelMap.put("averageVision", averageVisionValue);
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
                x -> x.getMyopiaWarningLevel() == WarningLevel.ONE.code;
        Predicate<StatConclusion> levelTwoPredicate =
                x -> x.getMyopiaWarningLevel() == WarningLevel.TWO.code;
        Predicate<StatConclusion> levelThreePredicate =
                x -> x.getMyopiaWarningLevel() == WarningLevel.THREE.code;
        Map<String, Object> levelMap = composeLevelStat(
                name, statConclusions, levelOnePredicate, levelTwoPredicate, levelThreePredicate);
        return levelMap;
    }

    /**
     * 构建 学龄 预警级别 统计
     * @param name 标题
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeSchoolAgeWarningLevelStat(
            String name, List<StatConclusion> statConclusions) {
        long rowTotal = statConclusions.size();
        Long levelZeroNum = statConclusions.stream()
                                    .filter(x -> x.getWarningLevel() == WarningLevel.ZERO.code)
                                    .count();
        Long levelOneNum = statConclusions.stream()
                                   .filter(x -> x.getWarningLevel() == WarningLevel.ONE.code)
                                   .count();
        Long levelTwoNum = statConclusions.stream()
                                   .filter(x -> x.getWarningLevel() == WarningLevel.TWO.code)
                                   .count();
        Long levelThreeNum = statConclusions.stream()
                                     .filter(x -> x.getWarningLevel() == WarningLevel.THREE.code)
                                     .count();
        List<BasicStatParams> list = new ArrayList<BasicStatParams>() {
            {
                add(composeBasicParams(WarningLevel.ZERO.name(), levelZeroNum, rowTotal));
                add(composeBasicParams(WarningLevel.ONE.name(), levelOneNum, rowTotal));
                add(composeBasicParams(WarningLevel.TWO.name(), levelTwoNum, rowTotal));
                add(composeBasicParams(WarningLevel.THREE.name(), levelThreeNum, rowTotal));
            }
        };
        return new HashMap<String, Object>() {
            {
                put("name", name);
                put("rowTotal", rowTotal);
                put("list", list);
            }
        };
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
        Long levelOneNum = statConclusions.stream().filter(levelOnePredicate).count();
        Long levelTwoNum = statConclusions.stream().filter(levelTwoPredicate).count();
        Long levelThreeNum = statConclusions.stream().filter(levelThreePredicate).count();
        List<BasicStatParams> list = new ArrayList<BasicStatParams>() {
            {
                add(composeBasicParams(WarningLevel.ONE.name(), levelOneNum, rowTotal));
                add(composeBasicParams(WarningLevel.TWO.name(), levelTwoNum, rowTotal));
                add(composeBasicParams(WarningLevel.THREE.name(), levelThreeNum, rowTotal));
                add(composeBasicParams(
                        "levelTotal", levelOneNum + levelTwoNum + levelThreeNum, rowTotal));
            }
        };
        return new HashMap<String, Object>() {
            {
                put("name", name);
                put("rowTotal", rowTotal);
                put("list", list);
            }
        };
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
        Long typeFrameNum =
                statConclusions.stream()
                        .filter(x -> x.getGlassesType() == GlassesType.FRAME_GLASSES.code)
                        .count();
        Long typeContactLensNum =
                statConclusions.stream()
                        .filter(x -> x.getGlassesType() == GlassesType.CONTACT_LENS.code)
                        .count();
        Long typeOrthokeratologyNum =
                statConclusions.stream()
                        .filter(x -> x.getGlassesType() == GlassesType.ORTHOKERATOLOGY.code)
                        .count();
        Long typeNotWearingNum =
                statConclusions.stream()
                        .filter(x -> x.getGlassesType() == GlassesType.NOT_WEARING.code)
                        .count();
        List<BasicStatParams> list = new ArrayList<BasicStatParams>() {
            {
                add(composeBasicParams(GlassesType.FRAME_GLASSES.name(), typeFrameNum, rowTotal));
                add(composeBasicParams(
                        GlassesType.FRAME_GLASSES.name(), typeContactLensNum, rowTotal));
                add(composeBasicParams(
                        GlassesType.ORTHOKERATOLOGY.name(), typeOrthokeratologyNum, rowTotal));
                add(composeBasicParams(
                        GlassesType.NOT_WEARING.name(), typeNotWearingNum, rowTotal));
            }
        };
        return new HashMap<String, Object>() {
            {
                put("name", name);
                put("rowTotal", rowTotal);
                put("list", list);
            }
        };
    }
}

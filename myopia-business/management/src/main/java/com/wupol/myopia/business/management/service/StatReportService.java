package com.wupol.myopia.business.management.service;

import com.alibaba.fastjson.JSONPath;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterNotFoundException;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.common.constant.GlassesType;
import com.wupol.myopia.business.management.constant.GenderEnum;
import com.wupol.myopia.business.management.constant.GradeCodeEnum;
import com.wupol.myopia.business.management.constant.SchoolAge;
import com.wupol.myopia.business.management.constant.ScreeningResultPahtConst;
import com.wupol.myopia.business.management.constant.VisionCorrection;
import com.wupol.myopia.business.management.constant.WarningLevel;
import com.wupol.myopia.business.management.domain.dto.SchoolGradeItems;
import com.wupol.myopia.business.management.domain.dto.stat.BasicStatParams;
import com.wupol.myopia.business.management.domain.dto.stat.ClassStat;
import com.wupol.myopia.business.management.domain.dto.stat.TableBasicStatParams;
import com.wupol.myopia.business.management.domain.model.*;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.StatConclusion;
import com.wupol.myopia.business.management.domain.query.StatConclusionQuery;
import com.wupol.myopia.business.management.domain.vo.StatConclusionReportVo;
import com.wupol.myopia.business.management.domain.vo.VisionScreeningResultReportVo;
import com.wupol.myopia.business.management.util.StatUtil;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private SchoolGradeService schoolGradeService;

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
                            // if (gradeCode.equals(GradeCodeEnum.OTHER)) continue;
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
                            add(composeGenderVisionUncorrectedStat(
                                    schoolAgeName, schoolAgeMyopiaConclusions));
                        }
                        add(composeGenderVisionUncorrectedStat("total", myopiaConclusions));
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
                            add(composeGenderVisionUnderCorrectedStat(
                                    schoolAgeName, schoolAgeVisionCorrectionConclusions));
                        }
                        add(composeGenderVisionUnderCorrectedStat(
                                "total", visionCorrectionConclusions));
                    }
                };

        List<Map<String, Object>> schoolAgeWarningLevelTable =
                new ArrayList<Map<String, Object>>() {
                    {
                        for (String schoolAgeName : schoolAgeMap.keySet()) {
                            add(composeWarningLevelStat(
                                    schoolAgeName, schoolAgeMap.get(schoolAgeName)));
                        }
                        add(composeWarningLevelStat("total", validConclusions));
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
                put("schoolAgeGlassesTypeDesc",
                        composeSchoolAgeGlassesTypeDesc(
                                "schoolAgeGlassesTypeTable", schoolAgeGlassesTypeTable));
                put("schoolAgeGenderVisionUncorrectedDesc",
                        composeSchoolAgeGenderVisionCorrectionDesc(
                                "schoolAgeGenderVisionUncorrectedTable",
                                schoolAgeGenderVisionUncorrectedTable));
                put("schoolAgeGenderVisionUnderCorrectedDesc",
                        composeSchoolAgeGenderVisionCorrectionDesc(
                                "schoolAgeGenderVisionUnderCorrectedTable",
                                schoolAgeGenderVisionUnderCorrectedTable));
                put("schoolAgeWarningLevelDesc",
                        composeSchoolAgeWarningLevelDesc(
                                "schoolAgeWarningLevelTable", schoolAgeWarningLevelTable));
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
        for (int i = 0; i < schoolAgeGenderVisionCorrectionTable.size(); i++) {
            Map<String, Object> item = schoolAgeGenderVisionCorrectionTable.get(i);
            String schoolAgeName = (String) item.get("name");
            if (schoolAgeName.equals("total")) {
                continue;
            }
            SchoolAge schoolAge = SchoolAge.valueOf(schoolAgeName);
            List<TableBasicStatParams> list = (List<TableBasicStatParams>) item.get("list");
            TableBasicStatParams params =
                    list.stream().filter(x -> x.getTitle().equals("total")).findFirst().get();
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
        Collections.sort(
                sortedList, Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
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
        Long totalNum = (Long) totalStat.get("rowTotal");
        List<BasicStatParams> totalRowList = (List<BasicStatParams>) totalStat.get("list");
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
                        warningLevelZeroSchoolAgeList.add(new BasicStatParams(
                                schoolAgeName, params.getRatio(), params.getNum()));
                        break;
                    case ONE:
                        warningLevelOneSchoolAgeList.add(new BasicStatParams(
                                schoolAgeName, params.getRatio(), params.getNum()));
                        break;
                    case TWO:
                        warningLevelTwoSchoolAgeList.add(new BasicStatParams(
                                schoolAgeName, params.getRatio(), params.getNum()));
                        break;
                    case THREE:
                        warningLevelThreeSchoolAgeList.add(new BasicStatParams(
                                schoolAgeName, params.getRatio(), params.getNum()));
                }
            }
        }
        Collections.sort(warningLevelZeroSchoolAgeList,
                Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        Collections.sort(warningLevelOneSchoolAgeList,
                Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        Collections.sort(warningLevelTwoSchoolAgeList,
                Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        Collections.sort(warningLevelThreeSchoolAgeList,
                Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        Map<String, Object> conclusionDesc = new HashMap<>();
        conclusionDesc.put("totalNum", totalNum);
        Long totalWarningNum = totalRowList.stream().mapToLong(x -> x.getNum()).sum();
        conclusionDesc.put("totalRatio", convertToPercentage(totalWarningNum * 1f / totalNum));
        List<ClassStat> levelList = new ArrayList<>();
        for (BasicStatParams params : totalRowList) {
            levelList.add(new ClassStat(params.getTitle(), params.getRatio(), params.getNum(),
                    warningLevelZeroSchoolAgeList));
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
     * 构造 学龄/视力情况 描述
     * @param title 标题
     * @param schoolAgeGlassesTypeTable  学龄眼镜类型表
     * @return
     */
    private Map<String, Object> composeSchoolAgeGlassesTypeDesc(
            String title, List<Map<String, Object>> schoolAgeGlassesTypeTable) {
        int size = schoolAgeGlassesTypeTable.size();
        List<BasicStatParams> schoolAgeMyopiaRatio = new ArrayList<>();
        Map<String, Object> conclusionDesc = new HashMap<>();
        Map<String, Object> totalStat = schoolAgeGlassesTypeTable.get(size - 1);
        List<BasicStatParams> list = (List<BasicStatParams>) totalStat.get("list");
        Long wearingNum = 0L;
        float wearingRatio = 0;
        for (BasicStatParams params : list) {
            if (!params.getTitle().equals(GlassesType.NOT_WEARING.name())) {
                wearingNum += params.getNum();
                wearingRatio += params.getRatio();
            }
        }
        Collections.sort(schoolAgeMyopiaRatio,
                Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        conclusionDesc.put("wearingNum", wearingNum);
        conclusionDesc.put("wearingRatio", wearingRatio);
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
     * @param srcScreeningNoticeId 通知ID
     * @param planId 计划ID
     * @param schoolId 学校ID
     * @return
     * @throws IOException
     */
    public Map<String, Object> getSchoolStatData(
            Integer srcScreeningNoticeId, Integer planId, int schoolId) throws IOException {
        Date startDate = null;
        Date endDate = null;
        Integer planStudentNum = 0;
        StatConclusionQuery query = new StatConclusionQuery();
        query.setSchoolId(schoolId);
        if (srcScreeningNoticeId != null) {
            ScreeningNotice notice = screeningNoticeService.getById(srcScreeningNoticeId);
            query.setSrcScreeningNoticeId(srcScreeningNoticeId);
            startDate = notice.getStartTime();
            endDate = notice.getEndTime();
            planStudentNum =screeningPlanSchoolStudentService.countPlanSchoolStudent(
                srcScreeningNoticeId, schoolId);
        }  else if (planId != null) {
            query.setPlanId(planId);
            ScreeningPlan sp = screeningPlanService.getById(planId);
            startDate = sp.getStartTime();
            endDate = sp.getEndTime();
            planStudentNum = sp.getStudentNumbers();
        }else{
            throw new ParameterNotFoundException("Parameters not illegal");
        }
        List<StatConclusion> statConclusions = statConclusionService.listByQuery(query);
        if (statConclusions == null) {
            return null;
        }
        School school = schoolService.getById(schoolId);
        String schoolName = school.getName();

        List<StatConclusion> firstScreenConclusions =
                statConclusions.stream()
                        .filter(x -> x.getIsRescreen() == false)
                        .collect(Collectors.toList());
        List<StatConclusion> validConclusions = firstScreenConclusions.stream()
                                                        .filter(x -> x.getIsValid() == true)
                                                        .collect(Collectors.toList());
        List<StatConclusion> maleList = validConclusions.stream()
                                                .filter(x -> x.getGender() == GenderEnum.MALE.type)
                                                .collect(Collectors.toList());
        List<StatConclusion> femaleList =
                validConclusions.stream()
                        .filter(x -> x.getGender() == GenderEnum.FEMALE.type)
                        .collect(Collectors.toList());

        long totalFirstScreeningNum = firstScreenConclusions.size();
        long validFirstScreeningNum = validConclusions.size();
        long myopiaNum = validConclusions.stream().filter(x -> x.getIsMyopia()).count();
        long lowVisionNum = validConclusions.stream().filter(x -> x.getIsLowVision()).count();
        long warningNum = validConclusions.stream()
                                  .filter(x -> x.getWarningLevel() != WarningLevel.NORMAL.code)
                                  .count();
        long warning0Num = validConclusions.stream().filter(x -> x.getWarningLevel() == 0).count();
        long warning1Num = validConclusions.stream().filter(x -> x.getWarningLevel() == 1).count();
        long warning2Num = validConclusions.stream().filter(x -> x.getWarningLevel() == 2).count();
        long warning3Num = validConclusions.stream().filter(x -> x.getWarningLevel() == 3).count();
        AverageVision averageVision = this.calculateAverageVision(validConclusions);
        float averageVisionValue =
                (averageVision.getAverageVisionLeft() + averageVision.getAverageVisionRight()) / 2;

        List<SchoolGradeItems> schoolGradeItems = schoolGradeService.getAllGradeList(schoolId);
        List<StatConclusionReportVo> statConclusionReportVos =
                statConclusionService.getReportVo(
                        srcScreeningNoticeId, planId, schoolId);
        Map<String, Object> resultMap = new HashMap<String, Object>() {
            {
                put("schoolName", schoolName);
                put("actualScreeningNum", totalFirstScreeningNum);
                put("validFirstScreeningNum", validFirstScreeningNum);
                put("maleNum", maleList.size());
                put("femaleNum", femaleList.size());
                put("myopiaRatio",

                        convertToPercentage(myopiaNum * 1f / validFirstScreeningNum));
                put("lowVisionRatio",
                        convertToPercentage(lowVisionNum * 1f / validFirstScreeningNum));
                put("averageVision", averageVisionValue);
                put("warningLevelStat", new ArrayList() {
                    {
                        add(new BasicStatParams("warningTotal",
                                convertToPercentage(

                                        warningNum * 1f / validFirstScreeningNum),
                                warningNum));
                        add(new BasicStatParams("warning0",
                                convertToPercentage(

                                        warning0Num * 1f / validFirstScreeningNum),
                                warning0Num));
                        add(new BasicStatParams("warning1",
                                convertToPercentage(

                                        warning1Num * 1f / validFirstScreeningNum),
                                warning1Num));
                        add(new BasicStatParams("warning2",
                                convertToPercentage(

                                        warning2Num * 1f / validFirstScreeningNum),
                                warning2Num));
                        add(new BasicStatParams("warning3",
                                convertToPercentage(

                                        warning3Num * 1f / validFirstScreeningNum),
                                warning3Num));
                    }
                });
                put("genderLowVisionLevelDesc", composeGenderLowVisionLevelDesc(validConclusions));
                put("schoolGradeLowVisionLevelDesc",
                        composeSchoolGradeLowVisionLevelDesc(validConclusions));
                put("schoolGradeMyopiaLevelDesc",
                        composeSchoolGradeMyopiaLevelDesc(validConclusions));
                put("schoolGradeClassLowVisionLevelTable",
                        composeSchoolGradeClassLowVisionLevelTable(
                                schoolGradeItems, validConclusions));
                put("schoolGradeClassMyopiaLevelTable",
                        composeSchoolGradeClassMyopiaLevelTable(
                                schoolGradeItems, validConclusions));
                put("genderMyopiaLevelDesc", composeGenderMyopiaLevelDesc(validConclusions));
                put("schoolGradeWearingTypeDesc",
                        composeSchoolGradeWearingTypeDesc(schoolGradeItems, validConclusions));
                put("schoolGradeGenderUncorrectedDesc",
                        composeSchoolGradeGenderUncorrectedDesc(
                                schoolGradeItems, validConclusions));
                put("schoolGradeGenderUnderCorrectedDesc",
                        composeSchoolGradeGenderUnderCorrectedDesc(
                                schoolGradeItems, validConclusions));
                put("schoolGradeWarningLevelDesc",
                        composeSchoolGradeWarningLevelDesc(schoolGradeItems, validConclusions));

                put("schoolClassStudentStatList",
                        composeSchoolClassStudentStatList(
                                 schoolId, schoolGradeItems, statConclusionReportVos));
            }
        };
        if (startDate != null) {
            resultMap.put("startDate", startDate.getTime());
        }
        if (endDate != null) {
            resultMap.put("endDate", endDate.getTime());
        }
        resultMap.put("planStudentNum", planStudentNum);
        return resultMap;
    }

    /**
     * 构建 学校每个班级的学生详情
     * @param screeningNoticeId 通知ID
     * @param schoolId 学校ID
     * @param schoolGradeItemList 学校班级列表
     * @return
     */
    private List<Map<String, List>> composeSchoolClassStudentStatList(
            int schoolId, List<SchoolGradeItems> schoolGradeItemList, List<StatConclusionReportVo> statConclusionReportVos) {
        List<Map<String, List>> schoolStudentStatList = new ArrayList<>();
        for (SchoolGradeItems schoolGradeItems : schoolGradeItemList) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeItems.getGradeCode());
            List<SchoolClass> schoolClasses = schoolGradeItems.getChild();
            List<Map<String, List>> schoolClassStatList = new ArrayList<>();
            for (SchoolClass schoolClass : schoolClasses) {
                List<StatConclusionReportVo> studentStatList =
                        statConclusionReportVos.stream()
                                .filter(x
                                        -> x.getSchoolGradeCode().equals(gradeCodeEnum.getCode())
                                                && x.getClassName().equals(schoolClass.getName()))
                                .collect(Collectors.toList());
                schoolClassStatList.add(new HashMap() {
                    {
                        put("name", schoolClass.getName());
                        put("list", genVisionScreeningResultReportVos(studentStatList));
                    }
                });
            }
            schoolStudentStatList.add(new HashMap() {
                {
                    put("name", gradeCodeEnum.name());
                    put("list", schoolClassStatList);
                }
            });
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
            List<SchoolGradeItems> schoolGradeItemList, List<StatConclusion> statConclusions) {
        List<Map<String, Object>> schoolGradeGenderVisionTable =
                new ArrayList<Map<String, Object>>();
        for (SchoolGradeItems schoolGradeItems : schoolGradeItemList) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeItems.getGradeCode());
            List<StatConclusion> list =
                    statConclusions.stream()
                            .filter(x -> x.getSchoolGradeCode().equals(gradeCodeEnum.getCode()))
                            .collect(Collectors.toList());
            schoolGradeGenderVisionTable.add(composeWarningLevelStat(gradeCodeEnum.name(), list));
        }
        Map<String, Object> totalStat = composeWarningLevelStat("total", statConclusions);
        schoolGradeGenderVisionTable.add(totalStat);
        List<BasicStatParams> totalStatList = (List<BasicStatParams>) totalStat.get("list");
        long totalWarningNum = totalStatList.stream().mapToLong(x -> x.getNum()).sum();
        long totalSize = statConclusions.size();
        return new HashMap<String, Object>() {
            {
                put("list", schoolGradeGenderVisionTable);
                put("totalStatList", totalStatList);
                put("totalWarningNum", totalWarningNum);
                put("totalWarningRatio", convertToPercentage(totalWarningNum * 1f / totalSize));
            }
        };
    }

    /**
     * 构建 年级 性别 视力未矫情况 统计
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeSchoolGradeGenderUncorrectedDesc(
            List<SchoolGradeItems> schoolGradeItemList, List<StatConclusion> statConclusions) {
        List<Map<String, Object>> schoolGradeGenderVisionTable =
                new ArrayList<Map<String, Object>>();
        for (SchoolGradeItems schoolGradeItems : schoolGradeItemList) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeItems.getGradeCode());
            List<StatConclusion> list =
                    statConclusions.stream()
                            .filter(x -> x.getSchoolGradeCode().equals(gradeCodeEnum.getCode()))
                            .collect(Collectors.toList());
            schoolGradeGenderVisionTable.add(
                    composeGenderVisionUncorrectedStat(gradeCodeEnum.name(), list));
        }
        Map<String, Object> totalStat =
                composeGenderVisionUncorrectedStat("total", statConclusions);
        schoolGradeGenderVisionTable.add(totalStat);
        List<BasicStatParams> totalStatList = (List<BasicStatParams>) totalStat.get("list");
        return new HashMap<String, Object>() {
            {
                put("list", schoolGradeGenderVisionTable);
                put("totalStatList", totalStatList);
            }
        };
    }

    /**
     * 构建 年级 性别 视力欠矫情况 统计
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeSchoolGradeGenderUnderCorrectedDesc(
            List<SchoolGradeItems> schoolGradeItemList, List<StatConclusion> statConclusions) {
        List<Map<String, Object>> schoolGradeGenderVisionTable =
                new ArrayList<Map<String, Object>>();
        for (SchoolGradeItems schoolGradeItems : schoolGradeItemList) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeItems.getGradeCode());
            List<StatConclusion> list =
                    statConclusions.stream()
                            .filter(x -> x.getSchoolGradeCode().equals(gradeCodeEnum.getCode()))
                            .collect(Collectors.toList());
            schoolGradeGenderVisionTable.add(
                    composeGenderVisionUnderCorrectedStat(gradeCodeEnum.name(), list));
        }
        Map<String, Object> totalStat =
                composeGenderVisionUnderCorrectedStat("total", statConclusions);
        schoolGradeGenderVisionTable.add(totalStat);
        List<BasicStatParams> totalStatList = (List<BasicStatParams>) totalStat.get("list");
        return new HashMap<String, Object>() {
            {
                put("list", schoolGradeGenderVisionTable);
                put("totalStatList", totalStatList);
            }
        };
    }

    /**
     * 构建 年级 戴镜情况 统计
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeSchoolGradeWearingTypeDesc(
            List<SchoolGradeItems> schoolGradeItemList, List<StatConclusion> statConclusions) {
        List<Map<String, Object>> schoolGradeWearingTypeTable =
                new ArrayList<Map<String, Object>>();
        for (SchoolGradeItems schoolGradeItems : schoolGradeItemList) {
            GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByCode(schoolGradeItems.getGradeCode());
            List<StatConclusion> list =
                    statConclusions.stream()
                            .filter(x -> x.getSchoolGradeCode().equals(gradeCodeEnum.getCode()))
                            .collect(Collectors.toList());
            schoolGradeWearingTypeTable.add(composeGlassesTypeStat(gradeCodeEnum.name(), list));
        }
        Map<String, Object> totalStat = composeGlassesTypeStat("total", statConclusions);
        schoolGradeWearingTypeTable.add(totalStat);

        long totalSize = statConclusions.size();
        Long totalWearingNum =
                statConclusions.stream()
                        .filter(x -> x.getGlassesType() != GlassesType.NOT_WEARING.code)
                        .count();
        List<BasicStatParams> totalStatList = (List<BasicStatParams>) totalStat.get("list");
        return new HashMap<String, Object>() {
            {
                put("list", schoolGradeWearingTypeTable);
                put("totalWearingNum", totalWearingNum);
                put("totalWearingRatio", convertToPercentage(totalWearingNum * 1f / totalSize));
                put("totalStatList", totalStatList);
            }
        };
    }

    /**
     * 构建 性别 视力低下 统计
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeGenderLowVisionLevelDesc(
            List<StatConclusion> statConclusions) {
        List<StatConclusion> maleList = statConclusions.stream()
                                                .filter(x -> x.getGender() == GenderEnum.MALE.type)
                                                .collect(Collectors.toList());
        List<StatConclusion> femaleList =
                statConclusions.stream()
                        .filter(x -> x.getGender() == GenderEnum.FEMALE.type)
                        .collect(Collectors.toList());
        Map<String, Object> maleStat = composeLowVisionLevelStat(GenderEnum.MALE.name(), maleList);
        Map<String, Object> femaleStat =
                composeLowVisionLevelStat(GenderEnum.FEMALE.name(), femaleList);
        Map<String, Object> totalStat = composeLowVisionLevelStat("total", statConclusions);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>() {
            {
                add(maleStat);
                add(femaleStat);
                add(totalStat);
            }
        };
        int totalSize = totalStat.size();
        List<BasicStatParams> totalLevelStat = (List<BasicStatParams>) totalStat.get("list");
        BasicStatParams lastTotalLevelStat = totalLevelStat.get(totalSize - 1);
        BasicStatParams topLowVisionRatioStat =
                totalLevelStat.subList(0, totalSize - 1)
                        .stream()
                        .max(Comparator.comparing(BasicStatParams::getRatio))
                        .get();
        // totalLevelStat.subList(0, totalLevelStat.size()-1).stream().max()
        return new HashMap<String, Object>() {
            {
                put("list", list);
                put("averageVision", totalStat.get("averageVision"));
                put("totalRatio", lastTotalLevelStat.getRatio());
                put("topStat", topLowVisionRatioStat);
            }
        };
    }

    /**
     * 构建 性别 近视 统计
     * @param statConclusions 统计数据
     * @return
     */
    private Map<String, Object> composeGenderMyopiaLevelDesc(List<StatConclusion> statConclusions) {
        List<StatConclusion> maleList = statConclusions.stream()
                                                .filter(x -> x.getGender() == GenderEnum.MALE.type)
                                                .collect(Collectors.toList());
        List<StatConclusion> femaleList =
                statConclusions.stream()
                        .filter(x -> x.getGender() == GenderEnum.FEMALE.type)
                        .collect(Collectors.toList());
        Map<String, Object> maleStat = composeMyopiaLevelStat(GenderEnum.MALE.name(), maleList);
        Map<String, Object> femaleStat =
                composeMyopiaLevelStat(GenderEnum.FEMALE.name(), femaleList);
        Map<String, Object> totalStat = composeMyopiaLevelStat("total", statConclusions);
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>() {
            {
                add(maleStat);
                add(femaleStat);
                add(totalStat);
            }
        };
        int totalSize = totalStat.size();
        List<BasicStatParams> totalLevelStat = (List<BasicStatParams>) totalStat.get("list");
        BasicStatParams lastTotalLevelStat = totalLevelStat.get(totalSize - 1);
        BasicStatParams topMyopiaRatioStat =
                totalLevelStat.subList(0, totalSize - 1)
                        .stream()
                        .max(Comparator.comparing(BasicStatParams::getRatio))
                        .get();
        // totalLevelStat.subList(0, totalLevelStat.size()-1).stream().max()
        return new HashMap<String, Object>() {
            {
                put("list", list);
                put("totalRatio", lastTotalLevelStat.getRatio());
                put("topStat", topMyopiaRatioStat);
            }
        };
    }

    /**
     * 构建 年级 视力低下 统计
     * @param statConclusions
     * @return
     */
    private Map<String, Object> composeSchoolGradeMyopiaLevelDesc(
            List<StatConclusion> statConclusions) {
        List<BasicStatParams> schoolGradeMyopiaRatioList = new ArrayList<>();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (GradeCodeEnum gradeCode : GradeCodeEnum.values()) {
            // if (gradeCode.equals(GradeCodeEnum.OTHER)) continue;
            Map<String, Object> lowVisionLevelStat = composeMyopiaLevelStat(gradeCode.name(),
                    statConclusions.stream()
                            .filter(x -> x.getSchoolGradeCode().equals(gradeCode.getCode()))
                            .collect(Collectors.toList()));
            list.add(lowVisionLevelStat);
            List<BasicStatParams> paramsList =
                    (List<BasicStatParams>) lowVisionLevelStat.get("list");
            schoolGradeMyopiaRatioList.add(new BasicStatParams(
                    gradeCode.name(), paramsList.get(paramsList.size() - 1).getRatio(), null));
        }
        list.add(composeMyopiaLevelStat("total", statConclusions));
        Collections.sort(schoolGradeMyopiaRatioList,
                Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        return new HashMap<String, Object>() {
            {
                put("list", list);
                put("sortedStat", schoolGradeMyopiaRatioList);
            }
        };
    }

    /**
     * 构建 年级 视力低下 统计
     * @param statConclusions
     * @return
     */
    private Map<String, Object> composeSchoolGradeLowVisionLevelDesc(
            List<StatConclusion> statConclusions) {
        List<BasicStatParams> schoolGradeLowVisionRatioList = new ArrayList<>();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (GradeCodeEnum gradeCode : GradeCodeEnum.values()) {
            // if (gradeCode.equals(GradeCodeEnum.OTHER)) continue;
            Map<String, Object> lowVisionLevelStat = composeLowVisionLevelStat(gradeCode.name(),
                    statConclusions.stream()
                            .filter(x -> x.getSchoolGradeCode().equals(gradeCode.getCode()))
                            .collect(Collectors.toList()));
            list.add(lowVisionLevelStat);
            List<BasicStatParams> paramsList =
                    (List<BasicStatParams>) lowVisionLevelStat.get("list");
            schoolGradeLowVisionRatioList.add(new BasicStatParams(
                    gradeCode.name(), paramsList.get(paramsList.size() - 1).getRatio(), null));
        }
        list.add(composeLowVisionLevelStat("total", statConclusions));
        Collections.sort(schoolGradeLowVisionRatioList,
                Comparator.comparingDouble(BasicStatParams::getRatio).reversed());
        return new HashMap<String, Object>() {
            {
                put("list", list);
                put("sortedStat", schoolGradeLowVisionRatioList);
            }
        };
    }

    /**
     * 构建 年级 班级 视力低下 统计
     * @param statConclusions
     * @return
     */
    private List<Map<String, Object>> composeSchoolGradeClassLowVisionLevelTable(
            List<SchoolGradeItems> schoolGradeItems, List<StatConclusion> statConclusions) {
        // List<Map<String, Object>> gradeList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> classList = new ArrayList<Map<String, Object>>();
        for (GradeCodeEnum gradeCode : GradeCodeEnum.values()) {
            SchoolGradeItems schoolGradeItem =
                    schoolGradeItems.stream()
                            .filter(x -> x.getGradeCode().equals(gradeCode.getCode()))
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
                                .filter(x -> x.getSchoolClassName().equals(schoolClass.getName()))
                                .collect(Collectors.toList()));
                lowVisionLevelStat.put("grade", gradeCode.name());
                lowVisionLevelStat.put("classNum", schoolClasses.size());
                classList.add(lowVisionLevelStat);
            }
            // gradeList.add(new HashMap() {
            //     {
            //         put("name", gradeCode.name());
            //         put("list", classList);
            //     }
            // });
        }
        return classList;
    }

    /**
     * 构建 年级 班级 近视 统计
     * @param statConclusions
     * @return
     */
    private List<Map<String, Object>> composeSchoolGradeClassMyopiaLevelTable(
            List<SchoolGradeItems> schoolGradeItems, List<StatConclusion> statConclusions) {
        // List<Map<String, Object>> gradeList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> classList = new ArrayList<Map<String, Object>>();
        for (GradeCodeEnum gradeCode : GradeCodeEnum.values()) {
            SchoolGradeItems schoolGradeItem =
                    schoolGradeItems.stream()
                            .filter(x -> x.getGradeCode().equals(gradeCode.getCode()))
                            .findFirst()
                            .orElse(null);
            if (schoolGradeItem == null) {
                continue;
            }

            List<SchoolClass> schoolClasses = schoolGradeItem.getChild();
            for (SchoolClass schoolClass : schoolClasses) {
                Map<String, Object> myopiaLevelStat = composeMyopiaLevelStat(schoolClass.getName(),
                        statConclusions.stream()
                                .filter(x -> x.getSchoolClassName().equals(schoolClass.getName()))
                                .collect(Collectors.toList()));
                myopiaLevelStat.put("grade", gradeCode.name());
                myopiaLevelStat.put("classNum", schoolClasses.size());
                classList.add(myopiaLevelStat);
            }
            // gradeList.add(new HashMap() {
            //     {
            //         put("name", gradeCode.name());
            //         put("list", classList);
            //     }
            // });
        }
        return classList;
        // return gradeList;
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
        Predicate<StatConclusion> predicate = x -> x.getIsMyopia();
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
                x -> x.getVisionCorrection() == VisionCorrection.UNCORRECTED.code;
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
                x -> x.getVisionCorrection() == VisionCorrection.UNDER_CORRECTED.code;
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
    private Map<String, Object> composeWarningLevelStat(
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

    /**
     * 生成筛查数据
     * @param statConclusionExportVos
     * @return
     */
    private List<VisionScreeningResultReportVo> genVisionScreeningResultReportVos(
            List<StatConclusionReportVo> statConclusionExportVos) {
        List<VisionScreeningResultReportVo> reportVos = new ArrayList<>();
        for (int i = 0; i < statConclusionExportVos.size(); i++) {
            StatConclusionReportVo vo = statConclusionExportVos.get(i);
            VisionScreeningResultReportVo reportVo = new VisionScreeningResultReportVo();
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
            StatConclusionReportVo vo, VisionScreeningResultReportVo reportVo) {
        reportVo.setNakedVisions(
                        eyeDateFormat((BigDecimal) JSONPath.eval(
                                              vo, ScreeningResultPahtConst.RIGHTEYE_NAKED_VISION),
                                (BigDecimal) JSONPath.eval(
                                        vo, ScreeningResultPahtConst.LEFTEYE_NAKED_VISION)))
                .setCorrectedVisions(
                        eyeDateFormat((BigDecimal) JSONPath.eval(vo,
                                              ScreeningResultPahtConst.RIGHTEYE_CORRECTED_VISION),
                                (BigDecimal) JSONPath.eval(
                                        vo, ScreeningResultPahtConst.LEFTEYE_CORRECTED_VISION)))
                .setSphs(eyeDateFormat(
                        (BigDecimal) JSONPath.eval(vo, ScreeningResultPahtConst.RIGHTEYE_SPH),
                        (BigDecimal) JSONPath.eval(vo, ScreeningResultPahtConst.LEFTEYE_SPH)))
                .setCyls(eyeDateFormat(
                        (BigDecimal) JSONPath.eval(vo, ScreeningResultPahtConst.RIGHTEYE_CYL),
                        (BigDecimal) JSONPath.eval(vo, ScreeningResultPahtConst.LEFTEYE_CYL)))
                .setAxials(eyeDateFormat(
                        (BigDecimal) JSONPath.eval(vo, ScreeningResultPahtConst.RIGHTEYE_AXIAL),
                        (BigDecimal) JSONPath.eval(vo, ScreeningResultPahtConst.LEFTEYE_AXIAL)))
                .setSphericalEquivalents(
                        eyeDateFormat(StatUtil.getSphericalEquivalent(
                                              (BigDecimal) JSONPath.eval(
                                                      vo, ScreeningResultPahtConst.RIGHTEYE_SPH),
                                              (BigDecimal) JSONPath.eval(
                                                      vo, ScreeningResultPahtConst.RIGHTEYE_CYL)),
                                StatUtil.getSphericalEquivalent(
                                        (BigDecimal) JSONPath.eval(
                                                vo, ScreeningResultPahtConst.LEFTEYE_SPH),
                                        (BigDecimal) JSONPath.eval(
                                                vo, ScreeningResultPahtConst.LEFTEYE_CYL))))
                .setLowVisionWarningLevel(vo.getNakedVisionWarningLevel())
                .setCorrectionType(vo.getCorrectionType());
    }

    /**
     * 眼别数据格式化
     * @param rightEyeData
     * @param leftEyeData
     * @return
     */
    private String eyeDateFormat(Number rightEyeData, Number leftEyeData) {
        return String.format("%s/%s", Objects.isNull(rightEyeData) ? "--" : rightEyeData,
                Objects.isNull(leftEyeData) ? "--" : leftEyeData);
    }
}

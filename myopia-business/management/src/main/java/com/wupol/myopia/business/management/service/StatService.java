package com.wupol.myopia.business.management.service;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.domain.CurrentUser;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.management.constant.GenderEnum;
import com.wupol.myopia.business.management.constant.SchoolAge;
import com.wupol.myopia.business.management.constant.StatClassLabel;
import com.wupol.myopia.business.management.domain.dto.stat.BasicStatParams;
import com.wupol.myopia.business.management.domain.dto.stat.ClassStat;
import com.wupol.myopia.business.management.domain.dto.stat.RescreenStat;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningClassStat;
import com.wupol.myopia.business.management.domain.dto.stat.ScreeningDataContrast;
import com.wupol.myopia.business.management.domain.dto.stat.WarningInfo;
import com.wupol.myopia.business.management.domain.dto.stat.WarningInfo.WarningLevelInfo;
import com.wupol.myopia.business.management.domain.model.District;
import com.wupol.myopia.business.management.domain.model.GovDept;
import com.wupol.myopia.business.management.domain.model.StatConclusion;
import com.wupol.myopia.business.management.domain.query.StatConclusionQuery;
import com.wupol.myopia.business.management.domain.vo.ScreeningDataContrastVo;
import com.wupol.myopia.business.management.facade.ExcelFacade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;

@Service
public class StatService {
    @Autowired
    private StatConclusionService statConclusionService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private GovDeptService govDeptService;

    @Autowired
    private ScreeningPlanService screeningPlanService;

    @Autowired
    ExcelFacade excelFacade;

    @Value("classpath:excel/ExportStatContrastTemplate.xls")
    private Resource exportStatContrastTemplate;

    /**
     * 预警信息
     * @return
     * @throws IOException
     */
    public WarningInfo getWarningList() throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<Integer> districtIds = this.getCurrentUserDistrictIds(currentUser);
        StatConclusionQuery lastOneQuery = new StatConclusionQuery();
        lastOneQuery.setDistrictIds(districtIds);
        lastOneQuery.setIsValid(true);
        lastOneQuery.setIsRescreen(false);
        StatConclusion lastConclusion = statConclusionService.getLastOne(lastOneQuery);
        if (lastConclusion == null) {
            return WarningInfo.builder().build();
        }
        LocalDate endDate = convertToLocalDate(lastConclusion.getCreateTime(), ZoneId.of("UTC+8"));
        LocalDate startDate = endDate.plusYears(-1);
        StatConclusionQuery warningListQuery = new StatConclusionQuery();
        warningListQuery.setDistrictIds(districtIds);
        warningListQuery.setIsValid(true);
        warningListQuery.setIsRescreen(false);
        warningListQuery.setStartTime(startDate);
        warningListQuery.setEndTime(endDate);
        List<StatConclusion> warningConclusions =
                statConclusionService.listByQuery(warningListQuery);
        long total = warningConclusions.size();
        long warning0Num = warningConclusions.stream().map(x -> x.getWarningLevel() == 0).count();
        long warning1Num = warningConclusions.stream().map(x -> x.getWarningLevel() == 1).count();
        long warning2Num = warningConclusions.stream().map(x -> x.getWarningLevel() == 2).count();
        long warning3Num = warningConclusions.stream().map(x -> x.getWarningLevel() == 3).count();
        long focusTargetsNum = warning1Num + warning2Num + warning3Num;
        return WarningInfo.builder()
                .statTime(startDate.toEpochDay())
                .endTime(endDate.toEpochDay())
                .focusTargetsNum(focusTargetsNum)
                .focusTargetsPercentage(convertToPercentage(focusTargetsNum * 1f / total))
                .warningLevelInfoList(new ArrayList<WarningLevelInfo>() {
                    {
                        add(new WarningLevelInfo(
                                0, warning0Num, convertToPercentage(warning0Num * 1f / total)));
                        add(new WarningLevelInfo(
                                1, warning1Num, convertToPercentage(warning1Num * 1f / total)));
                        add(new WarningLevelInfo(
                                2, warning2Num, convertToPercentage(warning2Num * 1f / total)));
                        add(new WarningLevelInfo(
                                3, warning3Num, convertToPercentage(warning3Num * 1f / total)));
                    }
                })
                .build();
    }

    /**
     * 对比统计数据
     * @param contrastTypeCode 对比类型
     * @param notificationId1 筛查通知ID_1
     * @param notificationId2 筛查通知ID_2
     * @param districtId 区域ID
     * @param schoolAge 学龄段
     * @return
     * @throws IOException
     */
    public Map<String, ScreeningDataContrast> getScreeningDataContrast(Integer notificationId1,
            Integer notificationId2, Integer districtId, Integer schoolAge) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        if (notificationId1 == null || notificationId1 < 0) {
            return null;
        }

        StatConclusionQuery query = new StatConclusionQuery();
        query.setSrcScreeningNoticeId(notificationId1);
        List<Integer> userDistrictIds = getCurrentUserDistrictIds(currentUser);
        if (districtId != null && districtId > 0) {
            List<District> districts =
                    districtService.getChildDistrictByParentCodePriorityCache(districtId);
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

        if (schoolAge != null && schoolAge > 0) {
            query.setDistrictId(schoolAge);
        }

        List<StatConclusion> resultConclusion1 = statConclusionService.listByQuery(query);
        int planScreeningNum =
                screeningPlanService.getScreeningPlanStudentNum(notificationId1, currentUser);

        ScreeningDataContrast data1 =
                composeScreeningDataContrast(resultConclusion1, planScreeningNum);

        Map<String, ScreeningDataContrast> result = new HashMap<String, ScreeningDataContrast>();
        result.put("result1", data1);
        if (notificationId2 != null && notificationId2 > 0) {
            int planScreeningNum2 =
                    screeningPlanService.getScreeningPlanStudentNum(notificationId2, currentUser);
            query.setSrcScreeningNoticeId(notificationId2);
            List<StatConclusion> resultConclusion2 = statConclusionService.listByQuery(query);
            result.put(
                    "result2", composeScreeningDataContrast(resultConclusion2, planScreeningNum2));
        }
        return result;
    }

    /**
     * 分类统计
     * @param notificationId 通知ID
     * @return
     * @throws IOException
     */
    public ScreeningClassStat getScreeningClassStat(Integer notificationId) throws IOException {
        CurrentUser currentUser = CurrentUserUtil.getCurrentUser();
        List<Integer> districtIds = currentUser.isPlatformAdminUser()
                ? null
                : this.getCurrentUserDistrictIds(currentUser);
        StatConclusionQuery query = new StatConclusionQuery();
        query.setDistrictIds(districtIds);
        query.setSrcScreeningNoticeId(notificationId);
        List<StatConclusion> statConclusions = statConclusionService.listByQuery(query);
        if (statConclusions == null) {
            return null;
        }

        List<StatConclusion> firstScreenConclusions =
                statConclusions.stream()
                        .filter(x -> x.getIsRescreen() == false)
                        .collect(Collectors.toList());

        List<StatConclusion> validConclusions = firstScreenConclusions.stream()
                                                        .filter(x -> x.getIsValid() == true)
                                                        .collect(Collectors.toList());

        List<StatConclusion> lowVisionConclusions = validConclusions.stream()
                                                            .filter(x -> x.getIsLowVision() == true)
                                                            .collect(Collectors.toList());

        List<StatConclusion> refractiveErrorConclusions =
                validConclusions.stream()
                        .filter(x -> x.getIsRefractiveError() == true)
                        .collect(Collectors.toList());

        List<StatConclusion> wearingGlassesConclusions =
                validConclusions.stream()
                        .filter(x -> x.getIsWearingGlasses() == true)
                        .collect(Collectors.toList());

        List<StatConclusion> myopiaConclusions = validConclusions.stream()
                                                         .filter(x -> x.getIsMyopia() == true)
                                                         .collect(Collectors.toList());

        long totalFirstScreeningNum = firstScreenConclusions.size();
        long validFirstScreeningNum = validConclusions.size();

        List<ClassStat> tabGender = new ArrayList<ClassStat>() {
            {
                add(composeGenderClassStat(
                        StatClassLabel.LOW_VISION, validFirstScreeningNum, lowVisionConclusions));
                add(composeGenderClassStat(StatClassLabel.REFRACTIVE_ERROR, validFirstScreeningNum,
                        refractiveErrorConclusions));
                add(composeGenderClassStat(
                        StatClassLabel.MYOPIA, validFirstScreeningNum, myopiaConclusions));
                add(composeGenderClassStat(StatClassLabel.WEARING_GLASSES, validFirstScreeningNum,
                        wearingGlassesConclusions));
            }
        };

        List<ClassStat> tabSchoolAge = new ArrayList<ClassStat>() {
            {
                add(composeSchoolAgeClassStat(
                        StatClassLabel.LOW_VISION, validFirstScreeningNum, lowVisionConclusions));
                add(composeSchoolAgeClassStat(StatClassLabel.REFRACTIVE_ERROR,
                        validFirstScreeningNum, refractiveErrorConclusions));
                add(composeSchoolAgeClassStat(
                        StatClassLabel.MYOPIA, validFirstScreeningNum, myopiaConclusions));
                add(composeSchoolAgeClassStat(StatClassLabel.WEARING_GLASSES,
                        validFirstScreeningNum, wearingGlassesConclusions));
            }
        };

        List<StatConclusion> rescreenConclusions =
                statConclusions.stream()
                        .filter(x -> x.getIsRescreen() == true && x.getIsValid() == true)
                        .collect(Collectors.toList());

        RescreenStat rescreenStat = this.composeRescreenConclusion(rescreenConclusions);
        AverageVision averageVision = this.calculateAverageVision(validConclusions);

        int planScreeningNum =
                screeningPlanService.getScreeningPlanStudentNum(notificationId, currentUser);
        return ScreeningClassStat.builder()
                .notificationId(notificationId)
                .screeningNum(planScreeningNum)
                .actualScreeningNum(totalFirstScreeningNum)
                .validScreeningNum(validFirstScreeningNum)
                .screeningFinishedRatio(
                        convertToPercentage(totalFirstScreeningNum * 1f / planScreeningNum))
                .averageVisionLeft(averageVision.getAverageVisionLeft())
                .averageVisionRight(averageVision.getAverageVisionRight())
                .tabGender(tabGender)
                .tabSchoolAge(tabSchoolAge)
                .rescreenStat(rescreenStat)
                .build();
    }

    public void exportStatContrast(Integer notificationId1, Integer notificationId2,
            Integer districtId, Integer schoolAge) throws IOException, UtilException {
        Map<String, ScreeningDataContrast> contrastResultMap =
                getScreeningDataContrast(notificationId1, notificationId2, districtId, schoolAge);
        ScreeningDataContrast result1 = contrastResultMap.get("result1");
        ScreeningDataContrast result2 = contrastResultMap.get("result2");
        List<ScreeningDataContrastVo> exportList = new ArrayList() {
            {
                add(composeScreeningDataContrastVo("对比项1", result1));
                add(composeScreeningDataContrastVo("对比项2", result2));
            };
        };
        excelFacade.exportStatContrast(CurrentUserUtil.getCurrentUser().getId(), exportList,
                exportStatContrastTemplate.getInputStream());
    }

    /**
     * 构造用于文件导出的对比筛查数据
     * @param title
     * @param contrast
     * @return
     */
    private ScreeningDataContrastVo composeScreeningDataContrastVo(
            String title, ScreeningDataContrast contrast) {
        if (contrast == null) {
            return null;
        }
        RescreenStat rs = contrast.getRescreenStat();
        return ScreeningDataContrastVo.builder()
                .title(title)
                .screeningNum(contrast.getScreeningNum())
                .actualScreeningNum(contrast.getActualScreeningNum())
                .averageVisionLeft(contrast.getAverageVisionLeft())
                .averageVisionRight(contrast.getAverageVisionRight())
                .lowVisionRatio(contrast.getLowVisionRatio())
                .refractiveErrorRatio(contrast.getRefractiveErrorRatio())
                .wearingGlassesRatio(contrast.getWearingGlassesRatio())
                .myopiaNum(contrast.getMyopiaNum())
                .myopiaRatio(contrast.getMyopiaRatio())
                .focusTargetsNum(contrast.getFocusTargetsNum())
                .warningLevelZeroRatio(contrast.getWarningLevelZeroRatio())
                .warningLevelOneRatio(contrast.getWarningLevelOneRatio())
                .warningLevelTwoRatio(contrast.getWarningLevelTwoRatio())
                .warningLevelThreeRatio(contrast.getWarningLevelThreeRatio())
                .recommendVisitNum(contrast.getRecommendVisitNum())
                .screeningFinishedRatio(contrast.getScreeningFinishedRatio())
                .rescreenNum(rs.getRescreenNum())
                .wearingGlassesRescreenNum(rs.getWearingGlassesRescreenNum())
                .wearingGlassesRescreenIndexNum(rs.getWearingGlassesRescreenIndexNum())
                .withoutGlassesRescreenNum(rs.getWithoutGlassesRescreenNum())
                .withoutGlassesRescreenIndexNum(rs.getWithoutGlassesRescreenIndexNum())
                .rescreenItemNum(rs.getRescreenItemNum())
                .incorrectItemNum(rs.getIncorrectItemNum())
                .incorrectRatio(rs.getIncorrectRatio())
                .build();
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
     * 按性别统计数据
     * @param label 统计标签
     * @param validScreeningNum 有效筛查统计总数
     * @param statConclusions 对应分类统计结论
     * @return
     */
    private ClassStat composeGenderClassStat(
            StatClassLabel label, long validScreeningNum, List<StatConclusion> statConclusions) {
        long statNum = statConclusions.size();
        long maleNum =
                statConclusions.stream().filter(x -> x.getGender() == GenderEnum.MALE.type).count();
        long femaleNum = statConclusions.size() - maleNum;
        return ClassStat.builder()
                .title(label.desc)
                .num(statNum)
                .ratio(convertToPercentage(statNum * 1f / validScreeningNum))
                .items(new ArrayList<BasicStatParams>() {
                    {
                        add(composeBasicParams(GenderEnum.MALE.name, maleNum, statNum));
                        add(composeBasicParams(GenderEnum.FEMALE.name, femaleNum, statNum));
                    }
                })
                .build();
    }

    /**
     * 按学龄统计数据
     * @param label 统计标签
     * @param validScreeningNum 实际有效筛查统计总数
     * @param statConclusions 对应分类统计结论
     * @return
     */
    private ClassStat composeSchoolAgeClassStat(
            StatClassLabel label, long validScreeningNum, List<StatConclusion> statConclusions) {
        long statNum = statConclusions.size();
        long kindergartenNum = statConclusions.stream()
                                       .filter(x -> x.getSchoolAge() == SchoolAge.KINDERGARTEN.code)
                                       .count();
        long primaryNum = statConclusions.stream()
                                  .filter(x -> x.getSchoolAge() == SchoolAge.PRIMARY.code)
                                  .count();
        long juniorNum = statConclusions.stream()
                                 .filter(x -> x.getSchoolAge() == SchoolAge.JUNIOR.code)
                                 .count();
        long highNum = statConclusions.stream()
                               .filter(x -> x.getSchoolAge() == SchoolAge.HIGH.code)
                               .count();
        long vocationalHighNum =
                statConclusions.stream()
                        .filter(x -> x.getSchoolAge() == SchoolAge.VOCATIONAL_HIGH.code)
                        .count();

        return ClassStat.builder()
                .title(label.desc)
                .num(statNum)
                .ratio(convertToPercentage(statNum * 1f / validScreeningNum))
                .items(new ArrayList<BasicStatParams>() {
                    {
                        add(composeBasicParams(
                                SchoolAge.KINDERGARTEN.desc, kindergartenNum, statNum));
                        add(composeBasicParams(SchoolAge.PRIMARY.desc, primaryNum, statNum));
                        add(composeBasicParams(SchoolAge.JUNIOR.desc, juniorNum, statNum));
                        add(composeBasicParams(SchoolAge.HIGH.desc, highNum, statNum));
                        add(composeBasicParams(
                                SchoolAge.VOCATIONAL_HIGH.desc, vocationalHighNum, statNum));
                    }
                })
                .build();
    }

    /**
     * 获取复查统计汇总
     * @param rescreenConclusions 复查统计记录
     * @return
     */
    private RescreenStat composeRescreenConclusion(List<StatConclusion> rescreenConclusions) {
        long totalScreeningNum = rescreenConclusions.size();
        long wearingGlassesNum =
                rescreenConclusions.stream().filter(x -> x.getIsWearingGlasses() == true).count();
        long wearingGlassesIndexNum = wearingGlassesNum * 6;
        long withoutGlassesNum = totalScreeningNum - wearingGlassesNum;
        long withoutGlassesIndexNum = withoutGlassesNum * 4;
        long errorIndexNum =
                rescreenConclusions.stream().mapToLong(x -> x.getRescreenErrorNum()).sum();
        return RescreenStat.builder()
                .rescreenNum(rescreenConclusions.size())
                .wearingGlassesRescreenNum(wearingGlassesNum)
                .wearingGlassesRescreenIndexNum(wearingGlassesIndexNum)
                .withoutGlassesRescreenNum(withoutGlassesNum)
                .withoutGlassesRescreenIndexNum(withoutGlassesIndexNum)
                .rescreenItemNum(wearingGlassesIndexNum + withoutGlassesIndexNum)
                .incorrectItemNum(errorIndexNum)
                .incorrectRatio(convertToPercentage(
                        errorIndexNum * 1f / (wearingGlassesIndexNum + withoutGlassesIndexNum)))
                .build();
    }

    /**
     * 构造对比数据
     * @param resultConclusion
     * @param planScreeningNum
     * @return
     */
    private ScreeningDataContrast composeScreeningDataContrast(
            List<StatConclusion> resultConclusion, int planScreeningNum) {
        List<StatConclusion> firstScreeningConclusions =
                resultConclusion.stream()
                        .filter(x -> x.getIsRescreen() == false)
                        .collect(Collectors.toList());
        List<StatConclusion> validConclusions = firstScreeningConclusions.stream()
                                                        .filter(x -> x.getIsValid() == true)
                                                        .collect(Collectors.toList());
        long lowVisionNum =
                validConclusions.stream().filter(x -> x.getIsLowVision() == true).count();
        long refractiveErrorNum =
                validConclusions.stream().filter(x -> x.getIsRefractiveError() == true).count();
        long wearingGlassesNum =
                validConclusions.stream().filter(x -> x.getIsWearingGlasses() == true).count();
        long myopiaNum = validConclusions.stream().filter(x -> x.getIsMyopia() == true).count();
        long totalFirstScreeningNum = firstScreeningConclusions.size();
        long validFirstScreeningNum = validConclusions.size();
        long recommendVisitNum =
                validConclusions.stream().filter(x -> x.getIsRecommendVisit() == true).count();
        long warning0Num = validConclusions.stream().map(x -> x.getWarningLevel() == 0).count();
        long warning1Num = validConclusions.stream().map(x -> x.getWarningLevel() == 1).count();
        long warning2Num = validConclusions.stream().map(x -> x.getWarningLevel() == 2).count();
        long warning3Num = validConclusions.stream().map(x -> x.getWarningLevel() == 3).count();

        List<StatConclusion> rescreenConclusions =
                resultConclusion.stream()
                        .filter(x -> x.getIsRescreen() == true && x.getIsValid() == true)
                        .collect(Collectors.toList());
        AverageVision averageVision = this.calculateAverageVision(validConclusions);
        RescreenStat rescreenStat = this.composeRescreenConclusion(rescreenConclusions);
        return ScreeningDataContrast.builder()
                .screeningNum(planScreeningNum)
                .actualScreeningNum(totalFirstScreeningNum)
                .averageVisionLeft(averageVision.getAverageVisionLeft())
                .averageVisionRight(averageVision.getAverageVisionRight())
                .lowVisionRatio(convertToPercentage(lowVisionNum * 1f / validFirstScreeningNum))
                .refractiveErrorRatio(
                        convertToPercentage(refractiveErrorNum * 1f / validFirstScreeningNum))
                .wearingGlassesRatio(
                        convertToPercentage(wearingGlassesNum * 1f / validFirstScreeningNum))
                .myopiaNum(myopiaNum)
                .myopiaRatio(convertToPercentage(myopiaNum * 1f / validFirstScreeningNum))
                .focusTargetsNum(warning1Num + warning2Num + warning3Num)
                .warningLevelZeroRatio(
                        convertToPercentage(warning0Num * 1f / validFirstScreeningNum))
                .warningLevelOneRatio(
                        convertToPercentage(warning1Num * 1f / validFirstScreeningNum))
                .warningLevelTwoRatio(
                        convertToPercentage(warning2Num * 1f / validFirstScreeningNum))
                .warningLevelThreeRatio(
                        convertToPercentage(warning3Num * 1f / validFirstScreeningNum))
                .recommendVisitNum(recommendVisitNum)
                .screeningFinishedRatio(
                        convertToPercentage(totalFirstScreeningNum * 1f / planScreeningNum))
                .rescreenStat(rescreenStat)
                .build();
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
     * 构造分类统计数据
     * @param desc 分类描述
     * @param statNum 分类统计数量
     * @param totalStatNum 统计总量
     * @return
     */
    private BasicStatParams composeBasicParams(String desc, long statNum, long totalStatNum) {
        return new BasicStatParams(
                desc, convertToPercentage(totalStatNum * 1f / totalStatNum), totalStatNum);
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

    /**
     * Date to LocalDate
     * @param date 日期
     * @param zoneId 时区ID
     * @return
     */
    private LocalDate convertToLocalDate(Date date, ZoneId zoneId) {
        Instant instant = date.toInstant();
        ZonedDateTime zdt = instant.atZone(zoneId);
        return zdt.toLocalDate();
    }

    @Data
    @Builder
    public static class AverageVision {
        private float averageVisionLeft;
        private float averageVisionRight;
    }
}

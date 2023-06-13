package com.wupol.myopia.business.api.management.service.report.plan.overview;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.RadioAndCount;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.SchoolAgeCount;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.overview.*;
import com.wupol.myopia.business.api.management.service.report.CommonReportService;
import com.wupol.myopia.business.api.management.util.GroupMapUtil;
import com.wupol.myopia.business.api.management.util.RadioAndCountUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 概述
 *
 * @author Simple4H
 */
@Service
public class OverviewPlanReportService {

    @Resource
    private CommonReportService commonReportService;

    /**
     * 获取概述
     *
     * @param plan               计划
     * @param planSchoolStudents 筛查学生
     * @param statConclusions    结论
     * @param planSchools        筛查学校
     * @return Overview
     */
    public Overview getOverview(ScreeningPlan plan, List<ScreeningPlanSchoolStudent> planSchoolStudents,
                                List<StatConclusion> statConclusions, List<ScreeningPlanSchool> planSchools) {

        Overview overview = new Overview();
        overview.setTitle(plan.getTitle());
        overview.setReportCreateTime(new Date());
        overview.setStartTime(plan.getStartTime());
        overview.setEndTime(plan.getEndTime());
        overview.setSchoolCount((long) planSchools.size());
        overview.setScreeningType(ScreeningTypeEnum.getByType(plan.getScreeningType()).getDesc());
        overview.setItems(GroupMapUtil.getSchoolAgeCount(planSchoolStudents, ScreeningPlanSchoolStudent::getSchoolId, s -> SchoolAge.get(s.getGradeType())));
        overview.setPlanScreeningCount((long) planSchoolStudents.size());
        overview.setUnScreeningCount((long) (planSchoolStudents.size() - statConclusions.size()));
        overview.setInvalidScreeningCount(statConclusions.stream().filter(s -> Objects.equals(s.getIsValid(), Boolean.FALSE)).count());
        overview.setValidScreeningCount(statConclusions.stream().filter(s -> Objects.equals(s.getIsValid(), Boolean.TRUE)).count());

        List<StatConclusion> validStatConclusions = statConclusions.stream().filter(s -> Objects.equals(s.getIsValid(), Boolean.TRUE)).collect(Collectors.toList());
        List<SchoolAgeCount> schoolAgeCount = GroupMapUtil.getSchoolAgeCount(validStatConclusions, StatConclusion::getSchoolId, s -> SchoolAge.get(s.getSchoolAge()));
        overview.setTable1(generateTable1(schoolAgeCount, validStatConclusions));
        overview.setTable2(generateTable2(validStatConclusions));
        overview.setTable3(generateTable3(validStatConclusions));
        overview.setTable4(generateTable4(schoolAgeCount, validStatConclusions));
        overview.setTable5(generateTable5(schoolAgeCount, validStatConclusions));
        return overview;
    }


    /**
     * 生成本次筛查对象分布
     *
     * @param schoolAgeCount       计划存在的学龄段
     * @param validStatConclusions 有效筛查数据
     * @return List<Table1>
     */
    private List<Table1> generateTable1(List<SchoolAgeCount> schoolAgeCount, List<StatConclusion> validStatConclusions) {
        Map<Integer, List<StatConclusion>> statConclusionMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));
        List<Table1> table1List = schoolAgeCount.stream().map(s -> {
            List<StatConclusion> statConclusions = statConclusionMap.get(s.getSchoolAge());
            return getTable1(s.getDesc(), statConclusions);
        }).collect(Collectors.toList());
        table1List.add(getTable1(CommonConst.TOTAL, validStatConclusions));
        return table1List;
    }

    /**
     * 获取本次筛查对象分布
     *
     * @param desc            描述
     * @param statConclusions 有效筛查数据
     * @return Table1
     */
    private Table1 getTable1(String desc, List<StatConclusion> statConclusions) {
        Table1 table1 = new Table1();
        table1.setDesc(desc);
        table1.setMale(countByGender(statConclusions, GenderEnum.MALE));
        table1.setFemale(countByGender(statConclusions, GenderEnum.FEMALE));
        table1.setTotal((long) statConclusions.size());
        return table1;
    }

    /**
     * 通过性别统计
     *
     * @param statConclusions 筛查数据
     * @param genderEnum      genderEnum
     * @return 统计
     */
    private Long countByGender(List<StatConclusion> statConclusions, GenderEnum genderEnum) {
        return statConclusions.stream().filter(s -> Objects.equals(s.getGender(), genderEnum.type)).count();
    }

    /**
     * 幼儿园儿童视力筛查情况
     *
     * @param validStatConclusions 有效筛查数据
     * @return Table2
     */
    private Table2 generateTable2(List<StatConclusion> validStatConclusions) {
        List<StatConclusion> kindergartenStatConclusion = commonReportService.getKindergartenStatConclusion(validStatConclusions);
        if (CollectionUtils.isEmpty(kindergartenStatConclusion)) {
            return null;
        }
        Table2 table2 = new Table2();
        table2.setValidCount((long) kindergartenStatConclusion.size());
        table2.setAvgVision(commonReportService.getAvgVision(kindergartenStatConclusion));
        table2.setLowVision(RadioAndCountUtil.getLowVisionRadioAndCount(kindergartenStatConclusion));
        table2.setRefractiveErrorVision(RadioAndCountUtil.getIsRefractiveErrorRadioAndCount(kindergartenStatConclusion));
        table2.setAnisometropia(RadioAndCountUtil.getIsAnisometropiaRadioAndCount(kindergartenStatConclusion));
        table2.setInsufficient(RadioAndCountUtil.getZeroSpRadioAndCount(kindergartenStatConclusion));
        return table2;
    }

    /**
     * 小学及以上教育阶段儿童青少年视力筛查情况
     *
     * @param validStatConclusions 有效筛查数据
     * @return Table3
     */
    private Table3 generateTable3(List<StatConclusion> validStatConclusions) {
        List<StatConclusion> primaryStatConclusion = commonReportService.getPrimaryStatConclusion(validStatConclusions);
        if (CollectionUtils.isEmpty(primaryStatConclusion)) {
            return null;
        }
        Table3 table3 = new Table3();
        table3.setValidCount((long) primaryStatConclusion.size());
        table3.setAvgVision(commonReportService.getAvgVision(primaryStatConclusion));
        table3.setMyopia(RadioAndCountUtil.getMyopiaRadioAndCount(primaryStatConclusion));
        table3.setLowVision(RadioAndCountUtil.getLowVisionRadioAndCount(primaryStatConclusion));
        table3.setUncorrected(RadioAndCountUtil.getUncorrectedDoctorRadioAndCount(primaryStatConclusion));
        table3.setUnder(RadioAndCountUtil.getUnderRadioAndCount(primaryStatConclusion));
        table3.setLightMyopia(RadioAndCountUtil.getLightMyopiaRadioAndCount(primaryStatConclusion));
        table3.setHighMyopia(RadioAndCountUtil.getHighMyopiaRadioAndCount(primaryStatConclusion));
        return table3;
    }


    /**
     * 小学及以上各教育阶段视力情况
     *
     * @param schoolAgeCount       计划存在的学龄段
     * @param validStatConclusions 有效筛查数据
     * @return Table4
     */
    private Table4 generateTable4(List<SchoolAgeCount> schoolAgeCount, List<StatConclusion> validStatConclusions) {
        Table4 table4 = new Table4();
        // 过滤幼儿园的
        table4.setTable(generateTable4Item(schoolAgeCount, validStatConclusions));
        return table4;
    }

    /**
     * 小学及以上各教育阶段视力情况
     *
     * @param schoolAgeCount       计划存在的学龄段
     * @param validStatConclusions 有效筛查数据
     * @return List<Table4.Table4Item>
     */
    private List<Table4.TableItem> generateTable4Item(List<SchoolAgeCount> schoolAgeCount, List<StatConclusion> validStatConclusions) {
        List<StatConclusion> primaryStatConclusion = commonReportService.getPrimaryStatConclusion(validStatConclusions);
        if (CollectionUtils.isEmpty(primaryStatConclusion)) {
            return null;
        }
        Map<Integer, List<StatConclusion>> statConclusionMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));
        List<Table4.TableItem> tableItemList = schoolAgeCount.stream().map(s -> {
            List<StatConclusion> statConclusions = statConclusionMap.get(s.getSchoolAge());
            return getTable4Item(s.getDesc(), statConclusions);
        }).collect(Collectors.toList());
        tableItemList.add(getTable4Item(CommonConst.TOTAL, validStatConclusions));
        return tableItemList;
    }

    /**
     * 小学及以上各教育阶段视力情况
     *
     * @param desc            描述
     * @param statConclusions 有效筛查数据
     * @return Table1
     */
    private Table4.TableItem getTable4Item(String desc, List<StatConclusion> statConclusions) {
        Table4.TableItem table4 = new Table4.TableItem();
        table4.setDesc(desc);
        table4.setValidCount((long) statConclusions.size());
        RadioAndCount myopiaRadioAndCount = RadioAndCountUtil.getMyopiaRadioAndCount(statConclusions);
        RadioAndCount lowVisionRadioAndCount = RadioAndCountUtil.getLowVisionRadioAndCount(statConclusions);
        table4.setMyopiaRadio(myopiaRadioAndCount.getRadio());
        table4.setMyopiaCount(myopiaRadioAndCount.getCount());
        table4.setLowVisionRadio(lowVisionRadioAndCount.getRadio());
        table4.setLowVisionCount(lowVisionRadioAndCount.getCount());
        return table4;
    }


    /**
     * 各教育阶段视力监测预警
     *
     * @param schoolAgeCount       计划存在的学龄段
     * @param validStatConclusions 有效筛查数据
     * @return Table4
     */
    private Table5 generateTable5(List<SchoolAgeCount> schoolAgeCount, List<StatConclusion> validStatConclusions) {
        Table5 table5 = new Table5();
        table5.setTable(generateTable5Item(schoolAgeCount, validStatConclusions));
        return table5;
    }

    /**
     * 各教育阶段视力监测预警
     *
     * @param schoolAgeCount       计划存在的学龄段
     * @param validStatConclusions 有效筛查数据
     * @return List<Table4.Table4Item>
     */
    private List<Table5.TableItem> generateTable5Item(List<SchoolAgeCount> schoolAgeCount, List<StatConclusion> validStatConclusions) {
        List<StatConclusion> primaryStatConclusion = commonReportService.getPrimaryStatConclusion(validStatConclusions);
        if (CollectionUtils.isEmpty(primaryStatConclusion)) {
            return null;
        }
        Map<Integer, List<StatConclusion>> statConclusionMap = validStatConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));
        List<Table5.TableItem> tableItemList = schoolAgeCount.stream().map(s -> {
            List<StatConclusion> statConclusions = statConclusionMap.get(s.getSchoolAge());
            return getTable5Item(s.getDesc(), statConclusions);
        }).collect(Collectors.toList());
        tableItemList.add(getTable5Item(CommonConst.TOTAL, validStatConclusions));
        return tableItemList;
    }

    /**
     * 各教育阶段视力监测预警
     *
     * @param desc            描述
     * @param statConclusions 有效筛查数据
     * @return Table5
     */
    private Table5.TableItem getTable5Item(String desc, List<StatConclusion> statConclusions) {
        Table5.TableItem tableItem = new Table5.TableItem();
        tableItem.setDesc(desc);
        tableItem.setValidCount((long) statConclusions.size());
        RadioAndCount recommendDoctorRadioAndCount = RadioAndCountUtil.getRecommendDoctorRadioAndCount(statConclusions);
        tableItem.setRecommendDoctorRadio(recommendDoctorRadioAndCount.getRadio());
        tableItem.setRecommendDoctorCount(recommendDoctorRadioAndCount.getCount());
        return tableItem;
    }
}

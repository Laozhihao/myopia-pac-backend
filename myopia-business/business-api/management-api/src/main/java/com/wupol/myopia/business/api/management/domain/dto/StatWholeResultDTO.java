package com.wupol.myopia.business.api.management.domain.dto;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.business.common.utils.constant.RatioEnum;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.time.DateUtils;

import java.util.*;

/**
 * @Author wulizhou
 * @Date 2021/6/9 11:31
 */
@Data
@Builder
@Accessors(chain = true)
public class StatWholeResultDTO {

    /**
     * 计划信息
     */
    private ScreeningPlan plan;
    /**
     * 学校数
     */
    private Integer schoolCount;

    private Long planStudentNum;
    private Long actualScreeningNum;
    private Long validFirstScreeningNum;
    private Float validRatio;

    /**
     * 学段学校分布
     */
    private Map<String, Long> schoolAgeDistribution;

    private List<TypeRatioDTO> myopia;
    private List<TypeRatioDTO> visionCorrection;
    private List<TypeRatioDTO> warnLevel;

    /**
     * 学校举例
     */
    private List<String> schoolExamples;

    /**
     * 学校人员
     */
    private List<StatSchoolPersonnelDTO> schoolPersonnel;

    /**
     * 学段人员信息
     */
    private List<StatSchoolAgePersonnelDTO> schoolAgePersonnel;

    /**
     * 性别近视情况
     */
    private List<TypeRatioDTO> genderMyopia;

    /**
     * 学段近视统计
     */
    private List<MyopiaDTO> schoolAgeMyopia;

    /**
     * 学校近视统计
     */
    private List<MyopiaDTO> schoolMyopia;

    /**
     * 年级近视统计
     */
    private List<MyopiaDTO> gradeMyopia;

    public static void main(String[] args) {

        Map<String, Long> schoolAgeDistribution = new HashMap<>();
        schoolAgeDistribution.put("KINDERGARTEN", 135L);
        schoolAgeDistribution.put("PRIMARY", 367L);
        schoolAgeDistribution.put("JUNIOR", 685L);
        schoolAgeDistribution.put("HIGH", 1234L);
        schoolAgeDistribution.put("VOCATIONAL_HIGH", 1234l);

        List<TypeRatioDTO> ratios = new ArrayList<>();
        ratios.add(TypeRatioDTO.getInstance(RatioEnum.MYOPIA.name(), 23, 23.88f));
        ratios.add(TypeRatioDTO.getInstance(RatioEnum.LOW_VISION.name(), 47, 29.88f));
        ratios.add(TypeRatioDTO.getInstance(RatioEnum.AVERAGE_VISION.name(), null, 73.88f));

        List<TypeRatioDTO> visionCorrection = new ArrayList<>();
        visionCorrection.add(TypeRatioDTO.getInstance(RatioEnum.UNCORRECTED.name(), 789, 4.88f));
        visionCorrection.add(TypeRatioDTO.getInstance(RatioEnum.WEARING_RATIO.name(), 39, 5.8f));
        visionCorrection.add(TypeRatioDTO.getInstance(RatioEnum.UNDER_CORRECTED.name(), 178, 24f));
        visionCorrection.add(TypeRatioDTO.getInstance(RatioEnum.ENOUGH_CORRECTED.name(), 389, 47f));

        List<TypeRatioDTO> warnLevel = new ArrayList<>();
        warnLevel.add(TypeRatioDTO.getInstance(RatioEnum.WARNING_LEVEL_0.name(), 36, 36f));
        warnLevel.add(TypeRatioDTO.getInstance(RatioEnum.WARNING_LEVEL_1.name(), 5, 5f));
        warnLevel.add(TypeRatioDTO.getInstance(RatioEnum.WARNING_LEVEL_2.name(), 4, 4f));
        warnLevel.add(TypeRatioDTO.getInstance(RatioEnum.WARNING_LEVEL_3.name(), 1, 1f));

        StatSchoolPersonnelDTO aa = (StatSchoolPersonnelDTO)new StatSchoolPersonnelDTO().setName("某学校").setPlanScreeningNum(200).setActualScreeningNum(100).setValidFirstScreeningNum(50);
        StatSchoolAgePersonnelDTO bb = (StatSchoolAgePersonnelDTO) new StatSchoolAgePersonnelDTO().setSchoolAge("KINDERGARTEN").setPlanScreeningNum(200).setActualScreeningNum(100).setValidFirstScreeningNum(50);

        MyopiaDTO m1 = (MyopiaDTO) new MyopiaDTO().setSchoolNum(5L).setStatNum(200).setKey("KINDERGARTEN").setNum(20).setRatio(20.00f);
        MyopiaDTO m2 = (MyopiaDTO) new MyopiaDTO().setStatNum(200).setNum(20).setKey("清化幼儿园").setRatio(20.00f);
        MyopiaDTO m3 = (MyopiaDTO) new MyopiaDTO().setStatNum(200).setNum(20).setKey("ONE_KINDERGARTEN").setRatio(20.00f);

        ScreeningPlan screeningPlan = new ScreeningPlan();
        screeningPlan.setId(1)
                .setSrcScreeningNoticeId(1)
                .setScreeningTaskId(1)
                .setTitle("这里标题")
                .setContent("这里内容")
                .setStartTime(new Date())
                .setEndTime(DateUtils.addDays(new Date(), 1))
                .setReleaseStatus(1);

        StatWholeResultDTO build = StatWholeResultDTO.builder()
                .plan(screeningPlan)
                .schoolCount(50)
                .planStudentNum(2000L)
                .actualScreeningNum(1000L)
                .validFirstScreeningNum(500L)
                .validRatio(50.00F)
                .schoolAgeDistribution(schoolAgeDistribution)
                .myopia(ratios)
                .visionCorrection(visionCorrection)
                .warnLevel(warnLevel)
                .schoolExamples(Arrays.asList("中华好学校", "第二学校", "第三学校"))
                .schoolPersonnel(Arrays.asList(aa))
                .schoolAgePersonnel(Arrays.asList(bb))
                .genderMyopia(null)
                .schoolAgeMyopia(Arrays.asList(m1))
                .schoolMyopia(Arrays.asList(m2))
                .gradeMyopia(Arrays.asList(m3))
                .build();

        System.out.println(JSON.toJSONString(build));

    }

}

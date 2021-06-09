package com.wupol.myopia.business.api.management.domain.dto;

import com.alibaba.fastjson.JSON;
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
    private Map<String, Integer> schoolAgeDistribution;

    /**
     * 平均视力
     */
    private Float averageVision;

    private NumAndRatio myopia;
    private NumAndRatio lowVision;
    private NumAndRatio w;
    private NumAndRatio d;
    private NumAndRatio q;
    private NumAndRatio z;
    private NumAndRatio l0;
    private NumAndRatio l1;
    private NumAndRatio l2;
    private NumAndRatio l3;

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
    private StatGenderMyopiaDTO genderMyopia;

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
        schoolAgeDistribution.put("VOCATIONAL_HIGH", 1234L);

        StatSchoolPersonnelDTO aa = (StatSchoolPersonnelDTO)new StatSchoolPersonnelDTO().setName("某学校").setPlanScreeningNum(200).setActualScreeningNum(100).setValidFirstScreeningNum(50);
        StatSchoolAgePersonnelDTO bb = (StatSchoolAgePersonnelDTO) new StatSchoolAgePersonnelDTO().setSchoolAge("KINDERGARTEN").setPlanScreeningNum(200).setActualScreeningNum(100).setValidFirstScreeningNum(50);
        StatGenderMyopiaDTO genderMyopia = new StatGenderMyopiaDTO();
        genderMyopia.setFemale(NumAndRatio.getInstance(20,10.00f))
                .setMale(NumAndRatio.getInstance(30, 23.23f));

        MyopiaDTO m1 = (MyopiaDTO) new MyopiaDTO().setName("KINDERGARTEN").setSchoolNum(5).setStatNum(200).setNum(20).setRatio(20.00f);
        MyopiaDTO m2 = (MyopiaDTO) new MyopiaDTO().setName("清化幼儿园").setStatNum(200).setNum(20).setRatio(20.00f);
        MyopiaDTO m3 = (MyopiaDTO) new MyopiaDTO().setName("ONE_KINDERGARTEN").setStatNum(200).setNum(20).setRatio(20.00f);

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
                .averageVision(23.89f)
                .myopia(NumAndRatio.getInstance(245, 22.78f))
                .lowVision(NumAndRatio.getInstance(245, 22.78f))
                .w(NumAndRatio.getInstance(245, 22.78f))
                .q(NumAndRatio.getInstance(216, 42.78f))
                .z(NumAndRatio.getInstance(445, 22.78f))
                .l0(NumAndRatio.getInstance(456, 28.78f))
                .l1(NumAndRatio.getInstance(245, 22.78f))
                .l2(NumAndRatio.getInstance(245, 22.78f))
                .l3(NumAndRatio.getInstance(245, 22.11f))
                .schoolExamples(Arrays.asList("中华好学校", "第二学校", "第三学校"))
                .schoolPersonnel(Arrays.asList(aa))
                .schoolAgePersonnel(Arrays.asList(bb))
                .genderMyopia(genderMyopia)
                .schoolAgeMyopia(Arrays.asList(m1))
                .schoolMyopia(Arrays.asList(m2))
                .gradeMyopia(Arrays.asList(m3))
                .build();

        System.out.println(JSON.toJSONString(build));

    }

}

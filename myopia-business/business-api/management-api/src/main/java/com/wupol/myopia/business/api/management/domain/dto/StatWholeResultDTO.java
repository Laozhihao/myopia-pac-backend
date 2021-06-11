package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

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
    private List<MyopiaDTO> genderMyopia;

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

}
